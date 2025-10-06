package com.gu.floodgate.schedule

import com.gu.floodgate.contentsource.ContentSourceService
import com.gu.floodgate.reindex.ReindexService
import com.gu.floodgate.schedule.ReindexSchedule.ScheduledReindexEvent
import com.typesafe.scalalogging.StrictLogging
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.extension.quartz.QuartzSchedulerTypedExtension

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

object ReindexSchedule {
  case class ScheduledReindexEvent()
}

/**
 * Manages the application's scheduled reindexes.
 */
class ReindexSchedule(contentSourceService: ContentSourceService, reindexService: ReindexService)(implicit ec: ExecutionContext) extends StrictLogging {
  private type Cron = String
  private val schedules: Map[Cron, ScheduleEvent] = Map(
    "0 0 0 ? * * *" -> new TagReindexScheduleEvent(contentSourceService, reindexService, "live-code"),
    "0 0 1 ? * * *" -> new TagReindexScheduleEvent(contentSourceService, reindexService, "live-prod")
  )

  private val minBackoff = 30.seconds
  private val maxBackoff = 5.minutes
  private val randomFactor = 0.2
  private val backoffSupervisorStrategy = SupervisorStrategy.restartWithBackoff(
    minBackoff,
    maxBackoff,
    randomFactor
  )

  private val scheduledReindexer = ActorSystem[Unit](schedulerGuardian(), "scheduled-reindexer")
  private val quartz = QuartzSchedulerTypedExtension(scheduledReindexer)

  def start(): Unit = {
    schedules.foreach {
      case (cronExpression, reindexScheduler) =>
        val actorName = getActorName(reindexScheduler.appName, reindexScheduler.environment)
        val reindexBehaviourWithBackoff = Behaviors
          .supervise(reindexScheduler.behavior)
          .onFailure(backoffSupervisorStrategy)

        val actor = scheduledReindexer.systemActorOf(reindexBehaviourWithBackoff, actorName)

        val firstScheduledDate = quartz.createTypedJobSchedule(
          name = actorName,
          receiver = actor,
          msg = ScheduledReindexEvent(),
          description = Some("Scheduled tag reindex"),
          cronExpression = cronExpression
        )

        logger.info(s"Creating reindex schedule for $actorName with cron '$cronExpression', first scheduled for ${firstScheduledDate.toString}. If the reindex fails, it will restart with a backoff strategy that operates a minimum of ${minBackoff.toString} and a maximum of ${maxBackoff.toString} after the first attempt.")
    }

    logger.info("All schedules created")
  }

  private def schedulerGuardian(): Behavior[Unit] = Behaviors.setup { context =>
    Behaviors.same
  }

  private val permittedActorChars = "[^A-Za-z-_.*$+:@&=,!~']"
  private def getActorName(appName: String, environment: String) = s"$appName-$environment".replaceAll(permittedActorChars, "_")
}
