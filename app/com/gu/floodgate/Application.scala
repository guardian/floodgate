package com.gu.floodgate

import play.api.mvc._

class Application extends Controller with AuthActions {

  def healthcheck = Action {
    Ok("ok")
  }

  /* Placeholder endpoint for the time being in order to implement auth */
  def fakeSecureRoute = AuthAction {
    Ok("If you're seeing this you managed to login successfully.")
  }

}