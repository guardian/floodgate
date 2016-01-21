package com.gu.floodgate.runningjob

import com.gu.floodgate.DynamoDBTable

import scala.concurrent.Future

class RunningJobService(runningJobTable: DynamoDBTable[RunningJob]) {

  def getRunningJobs(): Future[Seq[RunningJob]] = runningJobTable.getAll()
  def getRunningJob(id: String): Future[Option[RunningJob]] = runningJobTable.getItem(id)

}
