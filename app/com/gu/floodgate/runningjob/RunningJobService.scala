package com.gu.floodgate.runningjob

import com.gu.floodgate.{ RunningJobNotFound, CustomError, DynamoDBTable }
import org.scalactic.Or
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RunningJobService(runningJobTable: DynamoDBTable[RunningJob]) {

  def createRunningJob(runningJob: RunningJob) = runningJobTable.saveItem(runningJob)
  def updateRunningJob(id: String, environment: String, runningJob: RunningJob) = runningJobTable.updateItem(id, environment, runningJob)
  def getAllRunningJobs(): Future[Seq[RunningJob]] = runningJobTable.getAll()
  def getRunningJobsForContentSource(contentSourceId: String): Future[List[RunningJob]] = runningJobTable.getItems(contentSourceId)

  def getRunningJob(contentSourceId: String, contentSourceEnvironment: String): Future[RunningJob Or CustomError] = {
    runningJobTable.getItem(contentSourceId, contentSourceEnvironment) map { maybeRunningJob =>
      Or.from(
        option = maybeRunningJob,
        orElse = RunningJobNotFound(s"A running job for content source with id: $contentSourceId and environment: $contentSourceEnvironment does not exist.")
      )
    }
  }

  def removeRunningJob(id: String, environment: String) = runningJobTable.deleteItem(id, environment)

}
