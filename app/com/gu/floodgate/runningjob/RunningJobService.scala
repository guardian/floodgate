package com.gu.floodgate.runningjob

import cats.syntax.either._
import com.gu.floodgate._
import org.scanamo.DynamoFormat
import org.joda.time.{ DateTime, DateTimeZone }

import scala.concurrent.{Future, ExecutionContext}

class RunningJobService(runningJobTable: DynamoDBTable[RunningJob]) {

  implicit val dateFormat = DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](DateTime.parse(_).withZone(DateTimeZone.UTC))(_.toString)

  def createRunningJob(runningJob: RunningJob): Unit = runningJobTable.saveItem(runningJob)
  def updateRunningJob(id: String, environment: String, runningJob: RunningJob): Unit = runningJobTable.saveItem(runningJob)
  def removeRunningJob(id: String, environment: String): Unit = runningJobTable.deleteItem(id, environment)
  def getAllRunningJobs()(implicit ec: ExecutionContext): Future[Seq[RunningJob]] = runningJobTable.getAll()
  def getRunningJobsForContentSource(contentSourceId: String)(implicit ec: ExecutionContext): Future[List[RunningJob]] = runningJobTable.getItems(contentSourceId)

  def getRunningJob(contentSourceId: String, contentSourceEnvironment: String): Either[CustomError, RunningJob] =
    runningJobTable.getItem(contentSourceId, contentSourceEnvironment) match {
      case Some(result) => result.leftMap(err => ScanamoReadError(s"There was a DynamoRead error for content with id: $contentSourceId and environment: $contentSourceEnvironment: $err"))
      case None => Left(RunningJobNotFound(s"A running job for content source with id: $contentSourceId and environment: $contentSourceEnvironment does not exist."))
    }
}