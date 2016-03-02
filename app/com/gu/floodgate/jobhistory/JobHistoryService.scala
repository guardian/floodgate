package com.gu.floodgate.jobhistory

import cats.data.Validated
import com.gu.floodgate.DynamoDBTable
import com.gu.floodgate.reindex.ReindexStatus
import com.gu.floodgate.reindex.ReindexStatus.{ Unknown }
import com.gu.scanamo.DynamoFormat
import org.joda.time.DateTime

import scala.concurrent.Future

class JobHistoryService(jobHistoryTable: DynamoDBTable[JobHistory]) {

  implicit val dateFormat = DynamoFormat.xmap(DynamoFormat.stringFormat)(d => Validated.valid(new DateTime(d)))(_.toString)
  implicit val reindexStatusFormat = DynamoFormat.xmap(DynamoFormat.stringFormat)(s => Validated.valid(ReindexStatus.fromString(s).getOrElse(Unknown)))(ReindexStatus.asString(_))

  def createJobHistory(jobHistory: JobHistory): Unit = jobHistoryTable.saveItem(jobHistory)
  def getJobHistories(): Future[Seq[JobHistory]] = jobHistoryTable.getAll()
  def getJobHistoryForContentSource(contentSourceId: String, environment: String): Future[List[JobHistory]] =
    jobHistoryTable.getItemsWithFilter(contentSourceId, filterKeyName = "environment", filterValue = environment): Future[List[JobHistory]]

}
