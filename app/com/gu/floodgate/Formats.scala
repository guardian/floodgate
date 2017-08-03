package com.gu.floodgate

import com.gu.floodgate.contentsource._
import com.gu.floodgate.jobhistory._
import com.gu.floodgate.reindex.BulkJobActor.{ CompletedJobInfo, IsReindexing, PendingJobInfo, RunningJobInfo }
import com.gu.floodgate.reindex._
import com.gu.floodgate.runningjob._
import play.api.libs.json._
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

import scala.concurrent.Future

object Formats {

  implicit val dateParametersFormat = Json.format[DateParameters]
  implicit val runningJobFormat = Json.format[RunningJob]
  implicit val runningJobsResponseFormat = Json.format[RunningJobsResponse]
  implicit val singleRunningJobResponseFormat = Json.format[SingleRunningJobResponse]

  implicit val contentSourceSettingsFormat = Json.format[ContentSourceSettings]
  implicit val contentSourceFormat = Json.format[ContentSource]
  implicit val contentWithoutIdAndEnvironmentFormat = Json.format[ContentWithoutIdAndEnvironment]
  implicit val contentSourcesResponseFormat = Json.format[ContentSourcesResponse]
  implicit val singleContentSourceResponseFormat = Json.format[SingleContentSourceResponse]
  implicit val contentSourceWithoutIdFormat = Json.format[ContentSourceWithoutId]

  implicit object ReindexStatusWrites extends Writes[ReindexStatus] {
    def writes(status: ReindexStatus) = Json.toJson(ReindexStatus.asString(status))
  }

  implicit object ReindexStatusReads extends Reads[ReindexStatus] {
    def reads(statusJson: JsValue): JsResult[ReindexStatus] = statusJson match {
      case JsString(str) => {
        ReindexStatus.fromString(str).map { result =>
          JsSuccess(result)
        }.getOrElse(JsError("Unknown Reindex Status"))
      }
      case _ => JsError("Unknown Reindex Status")
    }
  }

  implicit val progressFormat = Json.format[Progress]
  implicit val jobHistoryFormat = Json.format[JobHistory]
  implicit val jobHistoriesResponseFormat = Json.format[JobHistoriesResponse]
  implicit val errorResponseFormat = Json.format[ErrorResponse]

  implicit val completedJobInfoFormat = Json.format[CompletedJobInfo]
  implicit val pendingJobInfoFormat = Json.format[PendingJobInfo]
  implicit val runningJobInfoFormat = Json.format[RunningJobInfo]
  implicit val isReindexingFormat = Json.format[IsReindexing]

}