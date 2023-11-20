package com.gu.floodgate.reindex

import org.apache.pekko.actor.{Actor, ActorLogging, Cancellable, Props}
import com.gu.floodgate.contentsource.ContentSource
import com.gu.floodgate.jobhistory.{JobHistory, JobHistoryService}
import com.gu.floodgate.reindex.ProgressTracker.{Cancel, TrackProgress, UpdateProgress}
import com.gu.floodgate.runningjob.{RunningJob, RunningJobService}
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.{DateTime, Minutes}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import com.gu.floodgate.Formats._

object ProgressTracker {

  def props(ws: WSClient, runningJobService: RunningJobService, jobHistoryService: JobHistoryService) =
    Props(new ProgressTracker(ws, runningJobService, jobHistoryService))

  case class TrackProgress(contentSource: ContentSource, runningJob: RunningJob)
  case class UpdateProgress(result: Try[WSResponse], contentSource: ContentSource, runningJob: RunningJob)
  case class Cancel(contentSource: ContentSource, runningJob: RunningJob)

}

class ProgressTracker(ws: WSClient, runningJobService: RunningJobService, jobHistoryService: JobHistoryService)
    extends Actor
    with ActorLogging
    with StrictLogging {
  import context.become
  import context.dispatcher

  private val FailedAttemptsToRetrieveProgressLimit = 30
  private val PollInterval = 2.seconds
  private var nextPollSchedule: Option[Cancellable] = None
  private var failedAttemptsToRetrieveProgress = 0

  final def receive = sleeping

  private def sleeping: Receive = {
    case TrackProgress(contentSource, runningJob) =>
      askForProgress(contentSource, runningJob)
      become(waitingForProgress)

    case Cancel(contentSource, runningJob) => completeProgressTracking(Cancelled, contentSource, runningJob)
    case other                             => logger.warn(s"Unexpected message received while sleeping: $other")
  }

  private def waitingForProgress: Receive = {
    case UpdateProgress(result, contentSource, runningJob) =>
      updateProgress(result, contentSource, runningJob)
      become(sleeping) // no matter whether the result is good or bad, we should go back to sleeping

    case Cancel =>
      become(waitingForFinalProgress) // don't stop ourselves until we have received the final response (just to avoid a dead letter)
    case other => logger.warn(s"Unexpected message received while waiting for response: $other")
  }

  private def waitingForFinalProgress: Receive = {
    case UpdateProgress(_, contentSource, runningJob) =>
      completeProgressTracking(Cancelled, contentSource, runningJob) // we've received our last message, so now we can stop (we ignore the response)
    case Cancel =>
      log.debug("Ignoring a Cancel message because we're already planning to stop after we receive the final response")
    case other => logger.warn(s"Unexpected message received while waiting for final response: $other")
  }

  private def askForProgress(contentSource: ContentSource, runningJob: RunningJob): Unit = {
    val myself = self // make this a val to fix its value, because the onComplete will occur later in a different thread
    ws.url(contentSource.reindexEndpoint).get() onComplete { result =>
      myself ! UpdateProgress(result, contentSource, runningJob)
    }
  }

  private def updateProgress(result: Try[WSResponse], contentSource: ContentSource, runningJob: RunningJob): Unit = {
    result match {
      case Success(response) =>
        response.status match {
          case 200 => onSuccess(response, contentSource, runningJob)
          case _   => {
            logger.warn(
              s"Content source with id: ${contentSource.id} returned a http ${response.status} response to a progress request: ${response.body}"
            )
            onFailure(contentSource, runningJob)
          }
        }
      case Failure(e) => {
        logger.error(
          s"Failure while calling content source: ${contentSource.id} for progress request: ${e.getMessage}"
        )
        onFailure(contentSource, runningJob)
      }
    }
  }

  private def onSuccess(response: WSResponse, contentSource: ContentSource, runningJob: RunningJob): Unit = {
    response.json
      .validate[Progress]
      .fold(
        error => {
          logger.warn(
            s"Content source with id: ${contentSource.id} appears to be returning progress updates in an incorrect format. Marking reindex as cancelled and stop monitoring reindex."
          )
          // TODO: try to cancel the reindex op on the source, rather than just stop paying any attention to it
          completeProgressTracking(Cancelled, contentSource, runningJob)
        },
        progress => actOnProgress(progress, contentSource, runningJob)
      )
  }

  private def onFailure(contentSource: ContentSource, runningJob: RunningJob): Unit = {
    if (FailedAttemptsToRetrieveProgressLimit == failedAttemptsToRetrieveProgress) {
      failedAttemptsToRetrieveProgress = 0
      logger.warn(s"Failing reindex after $failedAttemptsToRetrieveProgress failed attempts to get progress")
      completeProgressTracking(Failed, contentSource, runningJob)
    } else {
      failedAttemptsToRetrieveProgress += 1
      scheduleNextUpdate(contentSource, runningJob)
    }
  }

  private def actOnProgress(progress: Progress, contentSource: ContentSource, runningJob: RunningJob) = {
    progress.status match {
      case Completed =>
        val runningJobUpdate = RunningJob(
          runningJob.contentSourceId,
          runningJob.contentSourceEnvironment,
          progress.documentsIndexed,
          progress.documentsExpected,
          runningJob.startTime,
          runningJob.rangeFrom,
          runningJob.rangeTo
        )
        completeProgressTracking(Completed, contentSource, runningJobUpdate)

      case Failed => {
        logger.warn(s"Failing reindex after failed progress update from content source: " +
          s"Documents expected: ${progress.documentsExpected}, " +
          s"Documents indexed: ${progress.documentsIndexed}," +
          s"Status: ${progress.status}")
        completeProgressTracking(Failed, contentSource, runningJob)
      }

      case InProgress =>
        val runningJobUpdate = RunningJob(
          runningJob.contentSourceId,
          runningJob.contentSourceEnvironment,
          progress.documentsIndexed,
          progress.documentsExpected,
          runningJob.startTime,
          runningJob.rangeFrom,
          runningJob.rangeTo
        )

        runningJobService.updateRunningJob(
          runningJob.contentSourceId,
          runningJob.contentSourceEnvironment,
          runningJobUpdate
        )
        scheduleNextUpdate(contentSource, runningJob)

      case _ =>
        logger.warn(
          s"Incorrect status sent from client: ${progress.status.toString} for content source: ${contentSource.id}"
        )
    }
  }

  private def completeProgressTracking(
      status: ReindexStatus,
      contentSource: ContentSource,
      runningJob: RunningJob
  ): Unit = {

    def cleanupAndStop(): Unit = {
      nextPollSchedule foreach { _.cancel() } // cancel any schedule that we might have set up
      context.stop(self)
    }

    logger.info(s"Completing progress tracking for job from '${contentSource.id}' with reindex status: ${status}")
    val jobHistory = JobHistory(
      runningJob.contentSourceId,
      runningJob.startTime,
      new DateTime(),
      status,
      runningJob.contentSourceEnvironment,
      runningJob.rangeFrom,
      runningJob.rangeTo,
      Some(runningJob.documentsExpected),
      Some(runningJob.documentsIndexed)
    )
    runningJobService.removeRunningJob(runningJob.contentSourceId, runningJob.contentSourceEnvironment)
    jobHistoryService.createJobHistory(jobHistory)

    cleanupAndStop()
  }

  private def scheduleNextUpdate(contentSource: ContentSource, runningJob: RunningJob): Unit = {
    val schedule = context.system.scheduler.scheduleOnce(PollInterval, self, TrackProgress(contentSource, runningJob))
    nextPollSchedule = Some(schedule)
  }

}
