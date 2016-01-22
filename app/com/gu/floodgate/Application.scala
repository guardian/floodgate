package com.gu.floodgate

import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.Json
import play.api.mvc._
import play.json.extra.JsonFormat

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
  def fakeReindexRouteInitiate = Action {
    println("Reindex initiated.")
    Ok("")
  }
  
}