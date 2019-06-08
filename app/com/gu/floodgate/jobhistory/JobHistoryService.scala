package com.gu.floodgate.jobhistory

import com.gu.floodgate.DynamoDBTable
import com.gu.floodgate.reindex._
import org.scanamo.DynamoFormat
import org.scanamo.DynamoFormat._
import org.joda.time.{ DateTime, DateTimeZone }

import scala.concurrent.{ ExecutionContext, Future }

class JobHistoryService(jobHistoryTable: DynamoDBTable[JobHistory])(implicit ec: ExecutionContext) {

  implicit val dateFormat = DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](DateTime.parse(_).withZone(DateTimeZone.UTC))(_.toString)

  def createJobHistory(jobHistory: JobHistory): Unit = jobHistoryTable.saveItem(jobHistory)
  def getJobHistories()(implicit ec: ExecutionContext): Future[Seq[JobHistory]] = jobHistoryTable.getAll()
  def getJobHistoryForContentSource(contentSourceId: String, environment: String)(implicit ec: ExecutionContext): Future[List[JobHistory]] =
    jobHistoryTable.getItemsWithFilter(contentSourceId, filterKeyName = "environment", filterValue = environment)

  def getLatestJob(id: String, env: String)(implicit ec: ExecutionContext): Future[Option[JobHistory]] = jobHistoryTable.getLatestItem(id, filterKeyName = "environment", filterValue = env)

}