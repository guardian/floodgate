package com.gu.floodgate.runningjob

import play.api.mvc.{ Action, Controller }
import scala.concurrent.Future

class RunningJobApi(runningJobService: RunningJobService) extends Controller {

  def getRunningJobs = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

  def getRunningJob(id: String) = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

}
