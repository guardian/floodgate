package com.gu.floodgate.jobhistory

import com.gu.floodgate.DynamoDBTable
import com.gu.floodgate.reindex._
import com.gu.scanamo.DynamoFormat
import com.gu.scanamo.DynamoFormat._
import org.joda.time.{ DateTime, DateTimeZone }

import scala.concurrent.{ ExecutionContext, Future }

class JobHistoryService(jobHistoryTable: DynamoDBTable[JobHistory]) {

  implicit val dateFormat = DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](DateTime.parse(_).withZone(DateTimeZone.UTC))(_.toString)
  implicit val reindexStatusFormat = DynamoFormat.coercedXmap[ReindexStatus, String, IllegalArgumentException](s => ReindexStatus.fromString(s).getOrElse(Unknown))(ReindexStatus.asString)

  def createJobHistory(jobHistory: JobHistory): Unit = jobHistoryTable.saveItem[JobHistory](jobHistory)
  def getJobHistories(): Future[Seq[JobHistory]] = jobHistoryTable.getAll()
  def getJobHistoryForContentSource(contentSourceId: String, environment: String): Future[List[JobHistory]] =
    jobHistoryTable.getItemsWithFilter(contentSourceId, filterKeyName = "environment", filterValue = environment): Future[List[JobHistory]]

  def getLatestJob(id: String, env: String)(implicit ec: ExecutionContext): Future[Option[JobHistory]] = jobHistoryTable.getLatestItem(id, filterKeyName = "environment", filterValue = env)

}