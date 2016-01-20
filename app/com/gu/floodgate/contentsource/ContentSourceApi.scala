package com.gu.floodgate.contentsource

import com.gu.floodgate.ErrorResponse
import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ContentSourceApi(contentSourceService: ContentSourceService) extends Controller {

  def getContentSources = Action.async { implicit request =>
    contentSourceService.getContentSources() map { contentSources =>
      Ok(Json.toJson(ContentSourcesResponse(contentSources)))
    }
  }

  def getContentSource(id: String) = Action.async { implicit request =>
    contentSourceService.getContentSource(id) map { maybeContentSource =>
      maybeContentSource match {
        case Some(cs) => Ok(Json.toJson(SingleContentSourceResponse(cs)))
        case None => NotFound
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

  private val jsonError = Future.successful(BadRequest(Json.toJson(ErrorResponse("Invalid Json"))))

}
