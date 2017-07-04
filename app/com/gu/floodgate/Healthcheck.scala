package controllers

import play.api.mvc.legacy.Controller

class Healthcheck extends Controller {

  def healthcheck = Action {
    Ok("ok")
  }

}
