package com.gu.floodgate

import play.api.mvc._

class Application extends Controller {

  def healthcheck = Action {
    Ok("ok")
  }

}