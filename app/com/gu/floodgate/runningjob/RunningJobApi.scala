package com.gu.floodgate.runningjob

import com.gu.floodgate.ErrorResponse
import play.api.libs.json.Json
import play.api.mvc.legacy.Controller
import com.gu.floodgate.Formats._
import scala.concurrent.ExecutionContext

class RunningJobApi(runningJobService: RunningJobService)(implicit ec: ExecutionContext) extends Controller {

  def getAllRunningJobs = Action.async { implicit request =>
    runningJobService.getAllRunningJobs() map { runningJobs =>
      Ok(Json.toJson(RunningJobsResponse(runningJobs)))
    }
  }

  def getRunningJobs(contentSourceId: String) = Action.async { implicit request =>
    runningJobService.getRunningJobsForContentSource(contentSourceId) map { runningJobs =>
      Ok(Json.toJson(RunningJobsResponse(runningJobs)))
    }
  }

  def getRunningJob(contentSourceId: String, environment: String) = Action { implicit request =>
    runningJobService.getRunningJob(contentSourceId, environment) match {
      case Right(runningJob) => Ok(Json.toJson(SingleRunningJobResponse(runningJob)))
      case Left(error)       => NotFound(Json.toJson(ErrorResponse(error.message)))
    }
  }

}
