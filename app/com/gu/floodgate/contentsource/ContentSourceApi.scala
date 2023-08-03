package com.gu.floodgate.contentsource

import java.util.UUID

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout
import com.gu.floodgate.{BulkReindexInProcess, ErrorResponse}
import com.gu.floodgate.Formats._
import com.gu.floodgate.jobhistory.{JobHistoriesResponse, JobHistoryService}
import com.gu.floodgate.reindex.BulkJobActor._
import com.gu.floodgate.reindex.{DateParameters, ReindexService}
import com.gu.floodgate.runningjob.{RunningJobService, SingleRunningJobResponse}
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.legacy.Controller

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class ContentSourceApi(
    contentSourceService: ContentSourceService,
    reindexService: ReindexService,
    jobHistoryService: JobHistoryService,
    runningJobService: RunningJobService,
    bulkJobActorsMap: Map[String, ActorRef]
)(implicit ec: ExecutionContext)
    extends Controller
    with StrictLogging {

  implicit val timeout = Timeout(10.seconds)

  def getAllContentSources = Action.async { implicit request =>
    contentSourceService.getAllContentSources() map { contentSources =>
      Ok(Json.toJson(ContentSourcesResponse(contentSources)))
    }
  }

  def getContentSources(id: String) = Action.async { implicit request =>
    contentSourceService.getContentSources(id) map {
      case Right(cs)   => Ok(Json.toJson(ContentSourcesResponse(cs)))
      case Left(error) => NotFound(Json.toJson(ErrorResponse(error.message)))
    }
  }

  def getContentSource(id: String, environment: String) = Action { implicit request =>
    contentSourceService.getContentSource(id, environment) match {
      case Right(cs)   => Ok(Json.toJson(SingleContentSourceResponse(cs)))
      case Left(error) => NotFound(Json.toJson(ErrorResponse(error.message)))
    }
  }

  def createContentSources = Action.async(parse.json) { implicit request =>
    request.body
      .validate[List[ContentSourceWithoutId]]
      .fold(
        error => jsonError,
        contentSourcesWithoutId => {
          val id = UUID.randomUUID().toString
          val contentSourcesWithId = contentSourcesWithoutId.map(ContentSource(id, _))
          contentSourcesWithId.foreach(contentSourceService.createContentSource)
          Future.successful(Created)
        }
      )
  }

  def updateContentSource(id: String, environment: String) = Action.async(parse.json) { implicit request =>
    request.body
      .validate[ContentWithoutIdAndEnvironment]
      .fold(
        error => jsonError,
        contentSource => {
          val updatedContentSource = ContentSource(id, environment, contentSource)
          contentSourceService.updateContentSource(id, environment, updatedContentSource)
          Future.successful(Ok)
        }
      )
  }

  def deleteContentSource(id: String) = Action.async { implicit request =>
    contentSourceService.deleteContentSource(id)
    Future.successful(NoContent)

  }

  def reindex(id: String, environment: String, from: Option[String], to: Option[String]) = Action.async {
    implicit request =>
      def triggerReindex: Future[Result] = {
        DateParameters(from, to) match {
          case Right(dp: DateParameters) =>
            reindexService.reindex(id, environment, dp) map {
              case Right(runningJob) =>
                logger.info(s"Returning running job for: ${runningJob.contentSourceId}")
                Ok(Json.toJson(runningJob))
              case Left(customError) => {
                logger.warn(
                  s"Content source with id: $id returned custom error in response to a reindex request: ${customError.message}"
                )
                BadRequest(Json.toJson(ErrorResponse(customError.message)))
              }
            }
          case Left(error) => {
            logger.error(
              s"Content source with id: $id returned error in response to a reindex request: ${error.message}"
            )
            Future.successful(BadRequest(Json.toJson(ErrorResponse(error.message))))
          }
        }
      }

      val maybeActor = bulkJobActorsMap.get(environment)

      maybeActor match {
        case Some(actor) => {
          val futureActorStatus = (actor ? IsActorReindexing).mapTo[BulkReindexRequestResult]
          futureActorStatus.flatMap {
            case _: IsReindexing => {
              BulkReindexInProcess(
                s"Bulk reindex is in process for content source with id: $id and environment: $environment."
              )
              Future.successful(Ok(s"""{ "IsBulkReindexing": true}"""))
            }
            case NotReindexing => triggerReindex
            case _ => Future.successful(Ok(s"""{ "IsBulkReindexing": false}"""))
          }
        }
        case None => {
          ErrorResponse(s"No actor found for $environment. Running reindex for $id.")
          triggerReindex
        }
      }
  }

  def startBulkReindexer = Action.async(parse.json) { implicit request =>
    (request.body \ "environments")
      .validate[List[String]]
      .fold(
        error => jsonError,
        environments => {
          val actorList = environments.flatMap(env => bulkJobActorsMap.get(env))
          val bulkJobResponse =
            Future.sequence(actorList.map(actor => (actor ? StartBulkReindex).mapTo[BulkReindexRequestResult]))
          bulkJobResponse.map { resultList =>
            val failedOrSucceeded = environments.zip(resultList.map(resultObject => resultObject == CanTrigger)).toMap
            Ok(Json.toJson(failedOrSucceeded))
          }
        }
      )
  }

  def checkIfInBulkMode = Action.async { implicit request =>
    val actorList = bulkJobActorsMap.values.toList
    val actorEnvs = bulkJobActorsMap.keys.toList
    val bulkJobsActorStatus =
      Future.sequence(actorList.map(actor => (actor ? IsActorReindexing).mapTo[BulkReindexRequestResult]))
    bulkJobsActorStatus.map { actorStatusList =>
      val isReindexingList = actorEnvs.zip(actorStatusList).collect { case (s, status: IsReindexing) => (s, status) }
      isReindexingList match {
        case Nil => Ok(s"""{ "IsReindexing": false}""")
        case x => {
          val resultMap = (x).toMap
          Ok(s"""{ "IsReindexing": true, "data": ${Json.toJson(resultMap)} } """)
        }
      }
    }
  }

  def cancelPendingReindex(id: String, environment: String) = Action.async { implicit request =>
    bulkJobActorsMap.get(environment) match {
      case Some(actor) => {
        val futureActorResponse = (actor ? DropPendingJob(id, environment)).mapTo[DropPendingJobResult]
        futureActorResponse.map {
          case CancelledPendingJob => Ok(s"""{ "CancelledJob": true, "data": { "id": $id, "env": $environment}}""")
          case FailedToCancelPendingJob =>
            Ok(s"""{ "CancelledJob": false, "data": { "id": $id, "env": $environment}}""")
        }
      }
      case None => Future.successful(Ok(s"""{ "ActorUnavailable": $environment}"""))
    }
  }

  def cancelReindex(id: String, environment: String) = Action.async { implicit request =>
    reindexService.cancelReindex(id, environment: String) map {
      case Right(_)    => Ok
      case Left(error) => BadRequest(Json.toJson(ErrorResponse(error.message)))
    }
  }

  def getReindexHistory(id: String, environment: String) = Action.async { implicit request =>
    jobHistoryService.getJobHistoryForContentSource(id, environment) map { jobHistories =>
      Ok(Json.toJson(JobHistoriesResponse(jobHistories)))
    }
  }

  def getRunningReindex(id: String, environment: String) = Action { implicit request =>
    runningJobService.getRunningJob(id, environment) match {
      case Right(rj)   => Ok(Json.toJson(SingleRunningJobResponse(rj)))
      case Left(error) => NotFound(Json.toJson(ErrorResponse(error.message)))
    }
  }

  private val jsonError = Future.successful(BadRequest(Json.toJson(ErrorResponse("Invalid Json"))))

}
