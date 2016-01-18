package com.gu.floodgate.runningjob

import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller }
import scala.concurrent.ExecutionContext.Implicits.global

class RunningJobApi(runningJobService: RunningJobService) extends Controller {

  def getRunningJobs = Action.async { implicit request =>
    runningJobService.getRunningJobs() map { runningJobs =>
      Ok(Json.toJson(RunningJobsResponse(runningJobs)))
    }
  }

  def getRunningJob(id: String) = Action.async { implicit request =>
    runningJobService.getRunningJob(id) map { maybeRunningJob =>
      maybeRunningJob match {
        case Some(runningJob) => Ok(Json.toJson(SingleRunningJobResponse(runningJob)))
        case None => NotFound
      }
    }
  }

}
