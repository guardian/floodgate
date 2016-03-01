package com.gu.floodgate

import com.gu.floodgate.reindex.ReindexStatus.{ Completed, InProgress }
import com.gu.floodgate.reindex.{ ReindexStatus, Progress }
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.Json
import play.api.mvc._

class Application extends Controller with AuthActions with StrictLogging {

  def healthcheck = Action {
    Ok("ok")
  }

  def index = Action {
    Ok(views.html.app("Floodgate"))
  }

  /* Placeholder endpoint for the time being in order to implement auth */
  def fakeSecureRoute = AuthAction {
    Ok("If you're seeing this you managed to login successfully.")
  }

  /*
   * The below endpoints are provided to demonstrate the expected behavior of the endpoints implemented
   * on a content source. Tests against these, for the benefit of anyone implementing them in a new content source,
   * are provided in order to aid them.
   *
   */

  def fakeReindexRouteInitiate = Action { Ok("") }
  def fakeReindexRouteInitiateButInProgress = Action { Forbidden }
  def fakeReindexRouteCancel = Action { Ok("") }

  var progress = 0
  def fakeReindexRouteProgress = Action { implicit request =>
    println(s"Reindex progress.")
    progress = progress + 10
    if (progress == 100) {
      progress = 0
      Ok(Json.toJson(Progress(Completed, 100, 100)))
    } else {
      Ok(Json.toJson(Progress(InProgress, progress, 100)))
    }
  }

  def fakeReindexRouteProgressShowingCompleted = Action { Ok(Json.toJson(Progress(Completed, 100, 100))) }
  def fakeReindexRouteProgressButNeverRunBefore = Action { NotFound }

}