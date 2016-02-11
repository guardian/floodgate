package com.gu.floodgate.jobhistory

import com.gu.floodgate.DynamoDBTable

import scala.concurrent.Future

class JobHistoryService(jobHistoryTable: DynamoDBTable[JobHistory]) {

  def getJobHistories(): Future[Seq[JobHistory]] = jobHistoryTable.getAll()
  def getJobHistoryForContentSource(contentSourceId: String): Future[List[JobHistory]] = jobHistoryTable.getItems(contentSourceId)

}
