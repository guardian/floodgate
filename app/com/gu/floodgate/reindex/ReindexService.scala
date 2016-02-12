package com.gu.floodgate.reindex

import akka.actor.ActorRef
import com.gu.floodgate._
import com.gu.floodgate.contentsource.{ ContentSource, ContentSourceService }
import com.gu.floodgate.jobhistory.{ JobHistory, JobHistoryService }
import com.gu.floodgate.reindex.ProgressTrackerController.{ RemoveTracker, LaunchTracker }
import com.gu.floodgate.runningjob.{ RunningJob, RunningJobService }
import org.joda.time.DateTime
import org.scalactic.{ Bad, Good, Or }
import play.api.libs.ws.WSAPI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReindexService(contentSourceService: ContentSourceService,
    runningJobService: RunningJobService,
    jobHistoryService: JobHistoryService,
    reindexProgressMonitor: ActorRef,
    ws: WSAPI) {

  /**
   * @param id - id of content source to initiate reindex upon.
   */
  def reindex(id: String, dateParameters: DateParameters): Future[RunningJob Or CustomError] = {

    val futureContentSourceOrError = contentSourceService.getContentSource(id)

    isReindexRunning(id) flatMap { isRunning =>
      if (isRunning) {
        Future.successful(Bad(ReindexAlreadyRunning("A reindex is already running for this content source. Please try again once it has completed.")))
      } else {
        futureContentSourceOrError flatMap { contentSourceOrError =>
          contentSourceOrError match {
            case Good(cs) => initiateReindex(contentSource = cs, dateParameters)
            case Bad(error) => Future.successful(Bad(error).asOr)
          }
        }
      }
    }

  }

  /**
   * @param id - id of content source to initiate reindex upon.
   */
  def cancelReindex(id: String): Future[Happy Or CustomError] = {

    val futureContentSourceOrError = contentSourceService.getContentSource(id)
    futureContentSourceOrError flatMap { contentSourceOrError =>
      contentSourceOrError match {
        case Good(cs) => cancelReindex(contentSource = cs)
        case Bad(error) => Future.successful(Bad(error).asOr)
      }
    }

  }

  private def initiateReindex(contentSource: ContentSource, dateParameters: DateParameters): Future[RunningJob Or CustomError] = {
    val reindexUrl = buildUrl(contentSource.reindexEndpoint, dateParameters)
    ws.url(reindexUrl).post("") flatMap { response =>
      response.status match {
        case 200 =>
          // TODO move the interaction with RunningJobService into actor?
          val runningJob = RunningJob(contentSource.id)
          reindexProgressMonitor ! LaunchTracker(contentSource, runningJob)
          runningJobService.createRunningJob(runningJob) map (rj => Good(rj))

        case _ =>
          val error: CustomError = ReindexCannotBeInitiated(s"Could not initiate a reindex for ${contentSource.appName}")
          Future.successful(Bad(error))

      }
    }
  }

  private def cancelReindex(contentSource: ContentSource): Future[Happy Or CustomError] = {
    ws.url(contentSource.reindexEndpoint).delete flatMap { response =>
      response.status match {
        case 200 =>
          // TODO move the interaction with RunningJobService into actor?
          val futureRunningJobOrError = runningJobService.getRunningJob(contentSource.id)

          futureRunningJobOrError map { runningJobOrError =>
            runningJobOrError map { runningJob =>
              reindexProgressMonitor ! RemoveTracker(contentSource, runningJob)
              Happy()
            }
          }

        case _ =>
          val error: CustomError = CancellingReindexFailed(s"Could not cancel the current reindex for ${contentSource.appName}")
          Future.successful(Bad(error))

      }
    }
  }

  private def cancelReindex(contentSource: ContentSource): Future[Happy Or CustomError] = {
    ws.url(contentSource.reindexEndpoint).delete flatMap { response =>
      response.status match {
        case 200 => {
          /* TODO Remove running job and create job history item. */
          val runningJobOrError = runningJobService.getRunningJob(contentSource.id)

          runningJobOrError map { runningReindex =>

            val jobHistoryOrError = runningReindex.map(r => JobHistory(r.contentSourceId, r.startTime, new DateTime(), status = "cancelled"))

            jobHistoryOrError map { jobHistory =>
              runningJobService.removeRunningJob(jobHistory.contentSourceId)
              jobHistoryService.createJobHistory(jobHistory)
              Happy()
            }

          }

        }
        case _ => {
          val error: CustomError = CancellingReindexFailed(s"Could not cancel the current reindex for ${contentSource.appName}")
          Future.successful(Bad(error))
        }
      }
    }
  }

  private def isReindexRunning(contentSourceId: String): Future[Boolean] = runningJobService.getRunningJob(contentSourceId) map (_.isGood)

  private def buildUrl(endpoint: String, dateParameters: DateParameters): String = {
    (dateParameters.from, dateParameters.to) match {
      case (None, None) => endpoint
      case (Some(f), None) => s"$endpoint?from=${f.toString}"
      case (None, Some(t)) => s"$endpoint?to=${t.toString}"
      case (Some(f), Some(t)) => s"$endpoint?from=${f.toString}&to=${t.toString}"
    }
  }

}
