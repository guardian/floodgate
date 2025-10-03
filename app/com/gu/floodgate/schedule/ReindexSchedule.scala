package com.gu.floodgate.schedule

import com.gu.floodgate.reindex.ReindexService
import com.gu.floodgate.schedule.ReindexSchedule.ScheduledReindexEvent
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.extension.quartz.QuartzSchedulerTypedExtension

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object ReindexSchedule {
  case class ScheduledReindexEvent()
}

/**
 * Manages the application's scheduled reindexes.
 */
class ReindexSchedule(reindexService: ReindexService)(implicit ec: ExecutionContext) {
  private type Cron = String
  private val schedules: Map[Cron, ReindexScheduler] = Map(
    "0 0 0 ? * * *" -> new TagReindexScheduler(reindexService, "CODE"),
    "0 0 1 ? * * *" -> new TagReindexScheduler(reindexService, "PROD")
  )

  private val scheduledReindexer = ActorSystem[Unit](schedulerGuardian(), "scheduled-reindexer")
  private val quartz = QuartzSchedulerTypedExtension(scheduledReindexer)

  def start(): Unit = {
    schedules.foreach {
      case (cronExpression, reindexScheduler) =>
        val actorName = s"${reindexScheduler.id}-${reindexScheduler.environment}"
        val reindexBehaviourWithBackoff = Behaviors.supervise(reindexScheduler.behavior).onFailure(SupervisorStrategy.restartWithBackoff(minBackoff = 30.seconds, maxBackoff = 5.minutes, randomFactor = 0.2))
        val actor = scheduledReindexer.systemActorOf(reindexBehaviourWithBackoff, actorName)
        quartz.createTypedJobSchedule(
          name = actorName,
          receiver = actor,
          msg = ScheduledReindexEvent(),
          description = Some("Scheduled tag reindex"),
          cronExpression = cronExpression
        )
    }
  }

  private def schedulerGuardian(): Behavior[Unit] = Behaviors.setup { context =>
    Behaviors.same
  }
}
