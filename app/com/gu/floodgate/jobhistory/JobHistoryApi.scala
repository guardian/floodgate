package com.gu.floodgate.jobhistory

import play.api.libs.json.Json
import play.api.mvc.legacy.Controller
import com.gu.floodgate.Formats._
import scala.concurrent.ExecutionContext

class JobHistoryApi(jobHistoryService: JobHistoryService)(implicit ec: ExecutionContext) extends Controller {

  def getJobHistories = Action.async { implicit request =>
    jobHistoryService.getJobHistories() map { jobHistories =>
      Ok(Json.toJson(JobHistoriesResponse(jobHistories)))
    }
  }

}
