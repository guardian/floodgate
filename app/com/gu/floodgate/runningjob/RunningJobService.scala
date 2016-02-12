package com.gu.floodgate.runningjob

import com.gu.floodgate.{ RunningJobNotFound, CustomError, DynamoDBTable }
import org.scalactic.Or
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RunningJobService(runningJobTable: DynamoDBTable[RunningJob]) {

  def createRunningJob(runningJob: RunningJob) = runningJobTable.saveItem(runningJob)
  def getRunningJobs(): Future[Seq[RunningJob]] = runningJobTable.getAll()
  def getRunningJobsForContentSource(contentSourceId: String): Future[List[RunningJob]] = runningJobTable.getItems(contentSourceId)

  def getRunningJob(contentSourceId: String): Future[RunningJob Or CustomError] = {
    runningJobTable.getItem(contentSourceId, keyName = "contentSourceId") map { maybeRunningJob =>
      Or.from(
        option = maybeRunningJob,
        orElse = RunningJobNotFound(s"A running job for content source with id: $contentSourceId does not exist.")
      )
    }
  }

  def removeRunningJob(id: String) = runningJobTable.deleteItem(id)

}
