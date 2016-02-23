package com.gu.floodgate.reindex

import akka.actor.{ ActorRef, ActorLogging, Actor, Props }
import com.gu.floodgate.contentsource.ContentSource
import com.gu.floodgate.jobhistory.JobHistoryService
import com.gu.floodgate.reindex.ProgressTracker.{ Cancel, TrackProgress }
import com.gu.floodgate.reindex.ProgressTrackerController.{ RemoveTracker, LaunchTracker }
import com.gu.floodgate.runningjob.{ RunningJob, RunningJobService }
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.ws.WSAPI

object ProgressTrackerController {
  def props(ws: WSAPI, runningJobService: RunningJobService, jobHistoryService: JobHistoryService) = Props(new ProgressTrackerController(ws: WSAPI, runningJobService: RunningJobService, jobHistoryService))

  case class LaunchTracker(contentSource: ContentSource, runningJob: RunningJob)
  case class RemoveTracker(contentSource: ContentSource, runningJob: RunningJob)
}

class ProgressTrackerController(ws: WSAPI, runningJobService: RunningJobService, jobHistoryService: JobHistoryService) extends Actor with ActorLogging with StrictLogging {

  private var runningTrackers: Map[ContentSource, ActorRef] = Map.empty[ContentSource, ActorRef]

  def receive = {
    case LaunchTracker(contentSource, runningJob) => add(contentSource, runningJob)
    case RemoveTracker(contentSource, runningJob) => remove(contentSource, runningJob)
  }

  private def remove(contentSource: ContentSource, runningJob: RunningJob): Unit = {
    runningTrackers.get(contentSource.uniqueId).foreach {
      case tracker =>
        tracker ! Cancel(contentSource, runningJob)
        runningTrackers = runningTrackers - contentSource.uniqueId
    }
  }

  private def add(contentSource: ContentSource, runningJob: RunningJob): Unit = {
    val tracker = context.system.actorOf(ProgressTracker.props(ws, runningJobService, jobHistoryService), s"progress-tracker-${contentSource.uniqueId}")
    tracker ! TrackProgress(contentSource, runningJob)
    runningTrackers = runningTrackers + (contentSource.uniqueId -> tracker)
  }

}

