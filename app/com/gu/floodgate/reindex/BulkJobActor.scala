package com.gu.floodgate.reindex

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.pipe
import com.gu.floodgate.{ CustomError, RunningJobNotFound }
import com.gu.floodgate.contentsource.{ ContentSource, ContentSourceSettings }
import com.gu.floodgate.jobhistory.JobHistoryService
import com.gu.floodgate.reindex.BulkJobActor._
import com.gu.floodgate.runningjob.{ RunningJob, RunningJobService }
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object BulkJobActor {

  def props(contentSources: List[ContentSource], runningJobService: RunningJobService, reindexService: ReindexService, jobHistoryService: JobHistoryService) =
    Props(new BulkJobActor(contentSources, runningJobService, reindexService, jobHistoryService))

  //  Incoming messages
  case object StartBulkReindex
  case object IsActorReindexing
  case class DropPendingJob(id: String, env: String)

  case class RunningJobInfo(name: String, id: String, env: String, documentsIndexed: Int, documentsExpected: Int, startTime: DateTime, settings: ContentSourceSettings)
  case class PendingJobInfo(name: String, id: String, env: String)
  case class CompletedJobInfo(name: String, id: String, env: String, startTime: DateTime, finishTime: DateTime, status: ReindexStatus)

  //  Messages sent to controller
  sealed trait BulkReindexRequestResult
  case object CanTrigger extends BulkReindexRequestResult
  case object RejectJob extends BulkReindexRequestResult
  case class IsReindexing(runningJobs: Seq[RunningJobInfo], pendingJobs: List[PendingJobInfo], completedJobs: List[CompletedJobInfo]) extends BulkReindexRequestResult
  case object NotReindexing extends BulkReindexRequestResult

  sealed trait DropPendingJobResult
  case object CancelledPendingJob extends DropPendingJobResult
  case object FailedToCancelPendingJob extends DropPendingJobResult

  //  Messages sent to self
  case class ReindexStarted(id: String, environment: String)
  case object ReindexNext
  case class ReindexError(id: String, environment: String, error: CustomError)
  case class Polling(id: String, environment: String)

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
  private val idNameMap = orderedContentSources.map(source => (source.id, source.appName)).toMap
  private var pendingReindexes: List[ContentSource] = Nil // our queue
  private val PollInterval = 200.milliseconds

  final def receive: Receive = notReindexing

  private def notReindexing: Receive = {
    case StartBulkReindex => {
      if (noRunningReindexes) {
        pendingReindexes = orderedContentSources
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
      pendingReindexes = pendingReindexes.filterNot(source => source.id == id && source.environment == env)
      self ! Polling(id, env)
    }
    case ReindexError(id, env, error) => {
      pendingReindexes = Nil
      val failedReindex = contentSources.find(contentSource => (contentSource.id, contentSource.environment) == (id, env))
      logger.warn(s"Unable to trigger Reindex for $failedReindex")
      become(notReindexing)
    }
    case Polling(id, env) => pollRunningReindexes(id, env)
    case IsActorReindexing => buildReindexInfo pipeTo sender()
    case DropPendingJob(id, env) => {
      pendingReindexes = pendingReindexes.filterNot(source => source.id == id && source.environment == env)
      if (pendingReindexes.exists(source => source.id == id && source.environment == env) || runningJobService.getRunningJob(id, env).isRight) {
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

  private def getRunningJobs: Future[Seq[RunningJobInfo]] = {
    val env = orderedContentSources.map(source => source.environment).distinct
    val futureRunningJobs = runningJobService.getAllRunningJobs().map(jobs => jobs.filter(job => env.contains(job.contentSourceEnvironment)))
    futureRunningJobs.map { runningJobs =>
      runningJobs.map { job =>
        val name = idNameMap.getOrElse(job.contentSourceId, s"Name not found for ID: ${job.contentSourceId}")
        val contentSource = orderedContentSources.find(contentSource => (contentSource.id, contentSource.environment) == (job.contentSourceId, job.contentSourceEnvironment))
        contentSource match {
          case None => {
            logger.warn(s"Unable to find corresponding contentSource for $name. Set settings to false, you will be unable to cancel.")
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
  private def getCompletedJobs: Future[List[CompletedJobInfo]] = {

    val completedAndRunningSources = orderedContentSources.filterNot(source => pendingReindexes.contains(source))

    val result = Future.sequence(completedAndRunningSources.map { source =>
      jobHistoryService.getLatestJob(source.id, source.environment).map { maybeJobHistory =>
        maybeJobHistory.collect {
          case jh if runningJobService.getRunningJob(jh.contentSourceId, jh.environment).isLeft =>
            CompletedJobInfo(source.appName, source.id, source.environment, jh.startTime, jh.finishTime, jh.status)
        }
      }
    })

    result.map(l => l.flatten)
  }

  private def getPendingJobs: List[PendingJobInfo] = pendingReindexes.map(source => PendingJobInfo(source.appName, source.id, source.environment))

  private def reindexNextContentSource: Unit = {
    pendingReindexes match {
      case Nil => become(notReindexing)
      case source :: xs => {
        reindexService.reindex(source.id, source.environment, new DateParameters(None, None)).map {
          case Left(error) => self ! ReindexError(source.id, source.environment, error)
          case Right(runningJob) => self ! ReindexStarted(runningJob.contentSourceId, runningJob.contentSourceEnvironment)
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