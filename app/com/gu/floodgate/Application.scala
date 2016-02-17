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

  /* Mock endpoint acting as client for the time being in order to implement reindex */
  def fakeReindexRouteInitiate = Action { implicit request =>
    println(s"Reindex initiated: ${request.queryString}")
    Ok("")
  }

  /* Mock endpoint acting as client for the time being in order to implement reindex */
  def fakeReindexRouteCancel = Action { implicit request =>
    println(s"Reindex cancelled.")
    Ok("")
  }

  var progress = 0

  /* Mock endpoint acting as client for the time being in order to implement reindex */
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

}