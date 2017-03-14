package com.gu.floodgate.jobhistory

import com.gu.floodgate.DynamoDBTable
import com.gu.scanamo.DynamoFormat
import org.joda.time.{ DateTimeZone, DateTime }

import scala.concurrent.Future

class JobHistoryService(jobHistoryTable: DynamoDBTable[JobHistory]) {

  implicit val dateFormat = DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](DateTime.parse(_).withZone(DateTimeZone.UTC))(_.toString)

  def createJobHistory(jobHistory: JobHistory): Unit = jobHistoryTable.saveItem(jobHistory)
  def getJobHistories(): Future[Seq[JobHistory]] = jobHistoryTable.getAll()
  def getJobHistoryForContentSource(contentSourceId: String, environment: String): Future[List[JobHistory]] =
    jobHistoryTable.getItemsWithFilter(contentSourceId, filterKeyName = "environment", filterValue = environment): Future[List[JobHistory]]

}