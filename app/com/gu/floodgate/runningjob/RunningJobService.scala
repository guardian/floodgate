package com.gu.floodgate.runningjob

import cats.data.Validated
import com.gu.floodgate.{ RunningJobNotFound, CustomError, DynamoDBTable }
import com.gu.scanamo.DynamoFormat
import org.joda.time.DateTime
import org.scalactic.{ Bad, Good, Or }
import scala.concurrent.Future

class RunningJobService(runningJobTable: DynamoDBTable[RunningJob]) {

  implicit val dateFormat = DynamoFormat.xmap(DynamoFormat.stringFormat)(d => Validated.valid(new DateTime(d)))(_.toString)

  def createRunningJob(runningJob: RunningJob): Unit = runningJobTable.saveItem(runningJob)
  def updateRunningJob(id: String, environment: String, runningJob: RunningJob): Unit = runningJobTable.updateItem(id, environment, runningJob)
  def removeRunningJob(id: String, environment: String): Unit = runningJobTable.deleteItem(id, environment)
  def getAllRunningJobs(): Future[Seq[RunningJob]] = runningJobTable.getAll()
  def getRunningJobsForContentSource(contentSourceId: String): Future[List[RunningJob]] = runningJobTable.getItems(contentSourceId)

  def getRunningJob(contentSourceId: String, contentSourceEnvironment: String): RunningJob Or CustomError = {
    Or.from(
      option = runningJobTable.getItem[RunningJob](contentSourceId, contentSourceEnvironment).flatMap(_.toOption),
      orElse = RunningJobNotFound(s"A running job for content source with id: $contentSourceId and environment: $contentSourceEnvironment does not exist.")
    )
  }

}