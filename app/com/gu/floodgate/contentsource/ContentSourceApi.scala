package com.gu.floodgate.contentsource

import java.util.UUID

import com.gu.floodgate.ErrorResponse
import com.gu.floodgate.jobhistory.{ JobHistoriesResponse, JobHistoryService }
import com.gu.floodgate.reindex.{ DateParameters, ReindexService }
import com.gu.floodgate.runningjob.{ SingleRunningJobResponse, RunningJobsResponse, RunningJobService }
import org.scalactic.{ Bad, Good }
import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ContentSourceApi(contentSourceService: ContentSourceService,
    reindexService: ReindexService,
    jobHistoryService: JobHistoryService,
    runningJobService: RunningJobService) extends Controller {

  def getAllContentSources = Action.async { implicit request =>
    contentSourceService.getAllContentSources() map { contentSources =>
      Ok(Json.toJson(ContentSourcesResponse(contentSources)))
    }
  }

  def getContentSources(id: String) = Action.async { implicit request =>
    contentSourceService.getContentSources(id) map {
      case Good(cs) => Ok(Json.toJson(ContentSourcesResponse(cs)))
      case Bad(error) => NotFound(Json.toJson(ErrorResponse(error.message)))
    }
  }

  def getContentSource(id: String, environment: String) = Action { implicit request =>
    contentSourceService.getContentSource(id, environment) match {
      case Right(cs) => Ok(Json.toJson(SingleContentSourceResponse(cs)))
      case Left(error) => NotFound(Json.toJson(ErrorResponse(error.message)))
    }
  }

  def createContentSources = Action.async(parse.json) { implicit request =>
    request.body.validate[List[ContentSourceWithoutId]].fold(
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
    request.body.validate[ContentWithoutIdAndEnvironment].fold(
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

  def reindex(id: String, environment: String, from: Option[String], to: Option[String]) = Action.async { implicit request =>
    DateParameters(from, to) match {
      case Good(dp: DateParameters) =>
        reindexService.reindex(id, environment, dp) map {
          case Good(runningJob) => Ok(Json.toJson(runningJob))
          case Bad(error) => BadRequest(Json.toJson(ErrorResponse(error.message)))
        }
      case Bad(error) => Future.successful(BadRequest(Json.toJson(ErrorResponse(error.message))))
    }
  }

  def cancelReindex(id: String, environment: String) = Action.async { implicit request =>
    reindexService.cancelReindex(id, environment: String) map {
      case Right(_) => Ok
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
      case Right(rj) => Ok(Json.toJson(SingleRunningJobResponse(rj)))
      case Left(error) => NotFound(Json.toJson(ErrorResponse(error.message)))
    }
  }

  private val jsonError = Future.successful(BadRequest(Json.toJson(ErrorResponse("Invalid Json"))))

}
