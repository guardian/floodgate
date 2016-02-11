package com.gu.floodgate.contentsource

import com.gu.floodgate.ErrorResponse
import com.gu.floodgate.jobhistory.{ JobHistoriesResponse, JobHistoryService }
import com.gu.floodgate.reindex.{ ReindexService }
import org.scalactic.{ Bad, Good }
import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ContentSourceApi(contentSourceService: ContentSourceService, reindexService: ReindexService, jobHistoryService: JobHistoryService) extends Controller {

  def getContentSources = Action.async { implicit request =>
    contentSourceService.getContentSources() map { contentSources =>
      Ok(Json.toJson(ContentSourcesResponse(contentSources)))
    }
  }

  def getContentSource(id: String) = Action.async { implicit request =>
    contentSourceService.getContentSource(id) map { contentSourceOrError =>
      contentSourceOrError match {
        case Good(cs) => Ok(Json.toJson(SingleContentSourceResponse(cs)))
        case Bad(error) => NotFound(Json.toJson(ErrorResponse(error.message)))
      }
    }
  }

  def createContentSource = Action.async(parse.json) { implicit request =>
    request.body.validate[ContentSourceWithoutId].fold(
      error => jsonError,
      contentSourceWithoutId => {
        contentSourceService.createContentSource(ContentSource(contentSourceWithoutId))
        Future.successful(Created) // TODO might be beneficial to return created item.
      }
    )
  }

  def updateContentSource(id: String) = Action.async(parse.json) { implicit request =>
    request.body.validate[ContentSource].fold(
      error => jsonError,
      contentSource => {
        contentSourceService.updateContentSource(id, contentSource)
        Future.successful(Ok)
      }
    )
  }

  def deleteContentSource(id: String) = Action.async { implicit request =>
    contentSourceService.deleteContentSource(id)
    Future.successful(NoContent)
  }

  def reindex(id: String) = Action.async { implicit request =>
    reindexService.reindex(id) map { runningJobOrError =>
      runningJobOrError match {
        case Good(runningJob) => Ok(Json.toJson(runningJob))
        case Bad(error) => BadRequest(Json.toJson(ErrorResponse(error.message)))
      }
    }
  }

  def getReindexHistory(id: String) = Action.async { implicit request =>
    jobHistoryService.getJobHistoryForContentSource(id) map { jobHistories =>
      Ok(Json.toJson(JobHistoriesResponse(jobHistories)))
    }
  }

  private val jsonError = Future.successful(BadRequest(Json.toJson(ErrorResponse("Invalid Json"))))

}
