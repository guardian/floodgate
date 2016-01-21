package com.gu.floodgate.jobhistory

import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller }
import scala.concurrent.ExecutionContext.Implicits.global

class JobHistoryApi(jobHistoryService: JobHistoryService) extends Controller {

  def getJobHistories = Action.async { implicit request =>
    jobHistoryService.getJobHistories() map { jobHistories =>
      Ok(Json.toJson(JobHistoriesResponse(jobHistories)))
    }
  }

  def getJobHistory(id: String) = Action.async { implicit request =>
    jobHistoryService.getJobHistory(id) map { maybeJobHistory =>
      maybeJobHistory match {
        case Some(jobHistory) => Ok(Json.toJson(SingleJobHistoryResponse(jobHistory)))
        case None => NotFound
      }
    }
  }

}
