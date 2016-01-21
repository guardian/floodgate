package com.gu.floodgate.jobhistory

import com.gu.floodgate.DynamoDBTable

import scala.concurrent.Future

class JobHistoryService(jobHistoryTable: DynamoDBTable[JobHistory]) {

  def getJobHistories(): Future[Seq[JobHistory]] = jobHistoryTable.getAll()
  def getJobHistory(id: String): Future[Option[JobHistory]] = jobHistoryTable.getItem(id)

}
