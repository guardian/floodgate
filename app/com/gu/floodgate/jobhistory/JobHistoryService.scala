package com.gu.floodgate.jobhistory

import com.gu.floodgate.DynamoDBTable
import com.gu.floodgate.contentsource.ContentSource

import scala.concurrent.Future

class JobHistoryService(jobHistoryTable: DynamoDBTable[JobHistory]) {

  def createJobHistory(jobHistory: JobHistory) = jobHistoryTable.saveItem(jobHistory)
  def getJobHistories(): Future[Seq[JobHistory]] = jobHistoryTable.getAll()
  def getJobHistoryForContentSource(contentSourceId: String): Future[List[JobHistory]] = jobHistoryTable.getItems(contentSourceId)

}
