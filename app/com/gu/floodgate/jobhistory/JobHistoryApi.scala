package com.gu.floodgate.jobhistory

import play.api.libs.json.Json
import play.api.mvc.legacy.Controller
import scala.concurrent.ExecutionContext.Implicits.global
import com.gu.floodgate.Formats._

class JobHistoryApi(jobHistoryService: JobHistoryService) extends Controller {

  def getJobHistories = Action.async { implicit request =>
    jobHistoryService.getJobHistories() map { jobHistories =>
      Ok(Json.toJson(JobHistoriesResponse(jobHistories)))
    }
  }

}
