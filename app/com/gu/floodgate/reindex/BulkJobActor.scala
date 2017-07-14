package com.gu.floodgate.reindex

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.pipe
import com.gu.floodgate.{ CustomError, RunningJobNotFound }
import com.gu.floodgate.contentsource.{ ContentSource, ContentSourceSettings }
import com.gu.floodgate.jobhistory.JobHistoryService
import com.gu.floodgate.reindex.BulkJobActor._
import com.gu.floodgate.runningjob.{ RunningJob, RunningJobService }
import com.gu.scanamo.DynamoFormat
import com.typesafe.scalalogging.StrictLogging

import scala.util.{ Failure, Success }
import org.joda.time.{ DateTime, DateTimeZone }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object BulkJobActor {

  def props(contentSources: List[ContentSource], runningJobService: RunningJobService, reindexService: ReindexService, jobHistoryService: JobHistoryService) =
    Props(new BulkJobActor(contentSources, runningJobService, reindexService, jobHistoryService))

  sealed trait BulkReindexRequestResult
  sealed trait DropPendingJobResult
  case object CanTrigger extends BulkReindexRequestResult
  case object RejectJob extends BulkReindexRequestResult

  case object StartBulkReindex
  case object ReindexNext
  case object IsActorReindexing

  case class ReindexStarted(id: String, environment: String)
  case class ReindexError(id: String, environment: String, error: CustomError)
  case class Polling(id: String, environment: String)

  case class RunningJobInfo(name: String, id: String, env: String, documentsIndexed: Int, documentsExpected: Int, startTime: DateTime, settings: ContentSourceSettings)
  case class PendingJobInfo(name: String, id: String, env: String)
  case class CompletedJobInfo(name: String, id: String, env: String, startTime: DateTime, finishTime: DateTime, status: ReindexStatus)

  case class IsReindexing(runningJobs: Seq[RunningJobInfo], pendingJobs: List[PendingJobInfo], completedJobs: List[CompletedJobInfo]) extends BulkReindexRequestResult
  case object NotReindexing extends BulkReindexRequestResult
  case class DropPendingJob(id: String, env: String)
  case object CancelledPendingJob extends DropPendingJobResult
  case object FailedToCancelPendingJob extends DropPendingJobResult

}

class BulkJobActor(contentSources: List[ContentSource], runningJobService: RunningJobService, reindexService: ReindexService, jobHistoryService: JobHistoryService) extends Actor with ActorLogging with StrictLogging {

  import context.become

  private def sortContentToReindex: List[ContentSource] = {
    val helperMap = contentSources.map(content => content.appName -> content).toMap
    val intersection = desiredReindexOrder.intersect(helperMap.keys.toSeq)
    intersection.flatMap(contentType => helperMap.get(contentType))
  }

  private val desiredReindexOrder = List(
    "Atom Workshop",
    "Media Atom Maker",
    "Quizzes",
    "Interactive atoms",
    "Explainers",
    "Packages Editor",
    "Section Manager",
    "Tag Manager",
    "Flexible Content"
  )
  private val orderedContentSources = sortContentToReindex
  private val nameIdMap = orderedContentSources.map(source => (source.id, source.appName)).toMap
  private var pendingReindexes: List[(String, String)] = Nil // our queue
  private val PollInterval = 200.milliseconds

  final def receive: Receive = notReindexing

  private def notReindexing: Receive = {
    case StartBulkReindex => {
      if (noRunningReindexes) {
        pendingReindexes = orderedContentSources.map(contentSource => (contentSource.id, contentSource.environment))
        sender ! CanTrigger
        become(reindexing)
        self ! ReindexNext
      } else {
        sender ! RejectJob
      }
    }
    case IsActorReindexing => sender ! NotReindexing
    case other => logger.warn(s"Unexpected message received: $other")
  }

  private def reindexing: Receive = {
    case ReindexNext => reindexNextContentSource
    case ReindexStarted(id, env) => {
      pendingReindexes = pendingReindexes.filterNot(contentTuple => contentTuple == (id, env))
      self ! Polling(id, env)
    }
    case ReindexError(id, env, error) => {
      pendingReindexes = Nil
      val failedReindex = contentSources.find(contentSource => (contentSource.id, contentSource.environment) == (id, env))
      logger.warn(s"Unable to trigger Reindex for ${failedReindex}")
      become(notReindexing)
    }
    case Polling(id, env) => pollRunningReindexes(id, env)
    case IsActorReindexing => buildReindexInfo pipeTo sender()
    case DropPendingJob(id, env) => {
      pendingReindexes = pendingReindexes.filterNot(contentTuple => (id, env) == contentTuple)
      if (pendingReindexes.contains((id, env)) || runningJobService.getRunningJob(id, env).isRight) {
        sender ! FailedToCancelPendingJob
      } else {
        sender ! CancelledPendingJob
      }
    }
  }

  private def buildReindexInfo: Future[IsReindexing] = {

    val futureRunningJobs = getRunningJobs
    val futureCompletedJobs = getCompletedJobs

    for {
      runningJobs <- futureRunningJobs
      completedJobs <- futureCompletedJobs
      pendingJobs = getPendingJobs
    } yield {
      IsReindexing(runningJobs, pendingJobs, completedJobs)
    }
  }

  private def getRunningJobs = {
    val env = orderedContentSources.map(source => source.environment).distinct
    val futureRunningJobs = runningJobService.getAllRunningJobs().map(jobs => jobs.filter(job => env.contains(job.contentSourceEnvironment)))
    futureRunningJobs.map { runningJobs =>
      runningJobs.map { job =>
        val name = nameIdMap.get(job.contentSourceId).getOrElse(s"Name not found for ID: ${job.contentSourceId}")
        val contentSource = orderedContentSources.find(contentSource => (contentSource.id, contentSource.environment) == (job.contentSourceId, job.contentSourceEnvironment))
        contentSource match {
          case None => {
            logger.warn(s"Unable to find corresponding contentSource for ${name}. Set settings to false, you will be unable to cancel.")
            RunningJobInfo(
              name,
              job.contentSourceId,
              job.contentSourceEnvironment,
              job.documentsIndexed,
              job.documentsExpected,
              job.startTime,
              ContentSourceSettings(false, false)
            )
          }
          case Some(source) => RunningJobInfo(
            name,
            job.contentSourceId,
            job.contentSourceEnvironment,
            job.documentsIndexed,
            job.documentsExpected,
            job.startTime,
            source.contentSourceSettings
          )
        }
      }
    }
  }
  private def getCompletedJobs = {

    val pendingReindexesIds = pendingReindexes.map((tuple) => tuple._1).toSet
    val completedIds = orderedContentSources.map(source => source.id).filterNot(pendingReindexesIds)
    val completedContentSources = orderedContentSources.filter(contentSource => completedIds.contains(contentSource.id))

    val result = Future.sequence(completedContentSources.map { source =>
      jobHistoryService.getLatestJob(source.id, source.environment).map { maybeJobHistory =>
        maybeJobHistory.map { jobHistory =>
          CompletedJobInfo(source.appName, source.id, source.environment, jobHistory.startTime, jobHistory.finishTime, jobHistory.status)
        }
      }
    })

    result.map(list => list.flatten)

  }

  private def getPendingJobs = {
    val pendingReindexesSources = orderedContentSources.filter { contentSource =>
      val ids = pendingReindexes.flatMap(tuple => List(tuple._1))
      ids.contains(contentSource.id)
    }
    pendingReindexesSources.map(pendingSource => PendingJobInfo(pendingSource.appName, pendingSource.id, pendingSource.environment))
  }

  private def reindexNextContentSource: Unit = {
    pendingReindexes match {
      case Nil => become(notReindexing)
      case (id, env) :: xs => {
        reindexService.reindex(id, env, new DateParameters(None, None)).map {
          case Left(error) => self ! ReindexError(id, env, error)
          case Right(runningJob) => self ! ReindexStarted(id, env)
        }
      }
    }
  }

  private def pollRunningReindexes(id: String, environment: String): Unit = {
    runningJobService.getRunningJob(id, environment) match {
      case Left(RunningJobNotFound(_)) => self ! ReindexNext
      case Left(error: CustomError) => logger.warn(s"Job present in table, but failed to read: ${error.message}")
      case Right(job) => context.system.scheduler.scheduleOnce(PollInterval, self, Polling(id, environment))
    }
  }

  private def noRunningReindexes: Boolean = {
    val maybeRunningJob = contentSources.find { contentSource =>
      runningJobService.getRunningJob(contentSource.id, contentSource.environment) match {
        case Left(RunningJobNotFound(_)) => false
        case _ => true
      }
    }
    maybeRunningJob.isEmpty
  }

}