package com.gu.floodgate.reindex

import com.gu.floodgate.{ ReindexCannotBeInitiated, ReindexAlreadyRunning, CustomError }
import com.gu.floodgate.contentsource.{ ContentSource, ContentSourceService }
import com.gu.floodgate.runningjob.{ RunningJob, RunningJobService }
import org.scalactic.{ Bad, Good, Or }
import play.api.libs.ws.WSAPI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReindexService(contentSourceService: ContentSourceService, runningJobService: RunningJobService, ws: WSAPI) {

  /**
   * @param id - id of content source to intiate reindex upon.
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

  private def initiateReindex(contentSource: ContentSource, dateParameters: DateParameters): Future[RunningJob Or CustomError] = {
    val reindexUrl = buildUrl(contentSource.reindexEndpoint, dateParameters)
    ws.url(reindexUrl).post("") flatMap { response =>
      response.status match {
        case 200 => {
          runningJobService.createRunningJob(RunningJob(contentSource.id)) map (rj => Good(rj))
        }
        case _ => {
          val error: CustomError = ReindexCannotBeInitiated(s"Could not initiate a reindex for ${contentSource.appName}")
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
