package com.gu.floodgate.schedule

import com.gu.floodgate.contentsource.ContentSourceService
import com.gu.floodgate.reindex.ReindexService
import com.typesafe.scalalogging.StrictLogging
import org.apache.pekko.actor.typed.SupervisorStrategy.restart
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.extension.quartz.QuartzSchedulerTypedExtension

import scala.concurrent.ExecutionContext

sealed trait ReindexMessage

case class ScheduledReindexRequest() extends ReindexMessage
case class ScheduledReindexError(reason: String) extends ReindexMessage

/**
 * Manages the application's scheduled reindexes.
 */
class ReindexSchedule(contentSourceService: ContentSourceService, reindexService: ReindexService)(implicit ec: ExecutionContext) extends StrictLogging {
  private type Cron = String
  private val schedules: Map[Cron, ScheduleEvent] = Map(
    "0/30 * * ? * * *" -> new TagReindexScheduleEvent(contentSourceService, reindexService, "nope"),
    //    "0 0 1 ? * * *" -> new TagReindexScheduleEvent(contentSourceService, reindexService, "PROD")
  )

  private val scheduledReindexer = ActorSystem[Unit](schedulerGuardian(), "scheduled-reindexer")
  private val quartz = QuartzSchedulerTypedExtension(scheduledReindexer)

  def start(): Unit = {
    schedules.foreach {
      case (cronExpression, reindexScheduler) =>
        val actorName = getActorName(reindexScheduler.appName, reindexScheduler.environment)
        val reindexBehaviourWithRestart = Behaviors
          .supervise(reindexScheduler.behavior)
          .onFailure(restart)

        val actor = scheduledReindexer.systemActorOf(reindexBehaviourWithRestart, actorName)

        val firstScheduledDate = quartz.createTypedJobSchedule(
          name = actorName,
          receiver = actor,
          msg = ScheduledReindexRequest(),
          description = Some("Scheduled tag reindex"),
          cronExpression = cronExpression
        )

        logger.info(s"Creating reindex schedule for $actorName with cron '$cronExpression', first scheduled for ${firstScheduledDate.toString}")
    }

    logger.info("All schedules created")
  }

  private def schedulerGuardian(): Behavior[Unit] = Behaviors.setup { _ =>
    Behaviors.same
  }

  private val permittedActorChars = "[^A-Za-z-_.*$+:@&=,!~']"
  private def getActorName(appName: String, environment: String) = s"$appName-$environment".replaceAll(permittedActorChars, "_")
}
