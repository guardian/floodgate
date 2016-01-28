package com.gu.floodgate.runningjob

import com.gu.floodgate.ErrorResponse
import org.scalactic.{ Bad, Good }
import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller }
import scala.concurrent.ExecutionContext.Implicits.global

class RunningJobApi(runningJobService: RunningJobService) extends Controller {

  def getRunningJobs = Action.async { implicit request =>
    runningJobService.getRunningJobs() map { runningJobs =>
      Ok(Json.toJson(RunningJobsResponse(runningJobs)))
    }
  }

  def getRunningJob(contentSourceId: String) = Action.async { implicit request =>
    runningJobService.getRunningJob(contentSourceId) map { runningJobOrError =>
      runningJobOrError match {
        case Good(runningJob) => Ok(Json.toJson(SingleRunningJobResponse(runningJob)))
        case Bad(error) => NotFound(Json.toJson(ErrorResponse(error.message)))
      }
    }
  }

}
