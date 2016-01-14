package com.gu.floodgate.jobhistory

import play.api.mvc.{ Action, Controller }
import scala.concurrent.Future

class JobHistoryApi(jobHistoryService: JobHistoryService) extends Controller {

  def getJobHistories = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

  def getJobHistory(id: String) = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

}
