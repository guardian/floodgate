package com.gu.floodgate.reindex

import akka.actor.ActorRef
import cats.syntax.either._
import com.gu.floodgate._
import com.gu.floodgate.contentsource.{ ContentSource, ContentSourceService }
import com.gu.floodgate.jobhistory.JobHistoryService
import com.gu.floodgate.reindex.ProgressTrackerController.{ RemoveTracker, LaunchTracker }
import com.gu.floodgate.runningjob.{ RunningJob, RunningJobService }
import com.typesafe.scalalogging.StrictLogging
import org.scalactic.{ Bad, Good, Or }
import play.api.libs.ws.WSAPI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReindexService(contentSourceService: ContentSourceService,
    runningJobService: RunningJobService,
    jobHistoryService: JobHistoryService,
    reindexProgressMonitor: ActorRef,
    ws: WSAPI) extends StrictLogging {

  /**
   * @param id - id of content source to initiate reindex upon.
   */
  def reindex(id: String, environment: String, dateParameters: DateParameters): Future[RunningJob Or CustomError] = {
    val contentSourceOrError = contentSourceService.getContentSource(id, environment)
    val isRunning = isReindexRunning(id, environment)

    if (isRunning) {
      Future.successful(Bad(ReindexAlreadyRunning("A reindex is already running for this content source. Please try again once it has completed.")))
    } else {
      contentSourceOrError match {
        case Right(cs) => initiateReindex(contentSource = cs, dateParameters)
        case Left(error) => Future.successful(Bad(error).asOr)
      }
    }
  }

  /**
   * @param id - id of content source to initiate reindex upon.
   */
  def cancelReindex(id: String, environment: String): Future[Either[CustomError, Happy]] = {
    val contentSourceOrError = contentSourceService.getContentSource(id, environment)
    contentSourceOrError match {
      case Right(cs) => cancelReindex(contentSource = cs)
      case Left(error) => Future.successful(Left(error))
    }
  }

  private def initiateReindex(contentSource: ContentSource, dateParameters: DateParameters): Future[RunningJob Or CustomError] = {
    val reindexUrl: String = contentSource.reindexEndpointWithDateParams(dateParameters)
    println("=>>>", reindexUrl)
    println("ws", ws.url(reindexUrl))
    ws.url(reindexUrl).post("") flatMap { response =>
      println("response", response)
      response.status match {
        case 200 | 201 =>
          val runningJob = RunningJob(contentSource.id, contentSource.environment, dateParameters)
          reindexProgressMonitor ! LaunchTracker(contentSource, runningJob)
          runningJobService.createRunningJob(runningJob)
          Future.successful(Good(runningJob))

        case _ =>
          val error: CustomError = ReindexCannotBeInitiated(s"Could not initiate a reindex for ${contentSource.appName}.")
          Future.successful(Bad(error))
      }
    }
  }

  private def cancelReindex(contentSource: ContentSource): Future[Either[CustomError, Happy]] = {
    ws.url(contentSource.reindexEndpoint).delete map { response =>
      response.status match {
        case 200 =>
          val runningJobOrError = runningJobService.getRunningJob(contentSource.id, contentSource.environment)

          runningJobOrError map { runningJob =>
            reindexProgressMonitor ! RemoveTracker(contentSource, runningJob)
            Happy()
          }

        case _ =>
          val error: CustomError = CancellingReindexFailed(s"Could not cancel the current reindex for ${contentSource.appName}")
          Left(error)

      }
    }
  }

  private def isReindexRunning(contentSourceId: String, contentSourceEnvironment: String): Boolean = {
    runningJobService.getRunningJob(contentSourceId, contentSourceEnvironment).isRight
  }

}
