package com.gu.floodgate.schedule

import com.gu.floodgate.reindex.{DateParameters, ReindexService}
import com.typesafe.scalalogging.StrictLogging
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import scala.concurrent.ExecutionContext

class TagReindexScheduler(reindexService: ReindexService, val environment: String)(implicit ec: ExecutionContext) extends ReindexScheduler with StrictLogging {
    val id = "tagmanager"

    def behavior: Behavior[ReindexSchedule.ScheduledReindexEvent] = Behaviors.receive { (_, _) =>
      reindexService.reindex(id, environment, DateParameters(None, None)).map {
        case Right(runningJob) =>
          logger.info(s"Running scheduled reindex for service $id with job number ${runningJob.contentSourceId}")
        case Left(customError) =>
          logger.warn(
            s"Attempted to run scheduled reindex for service $id, but received error: ${customError.message}",
          )
          // Let the actor crash, the supervisor will apply the necessary strategy
          throw new Error(customError.message)
      }
      Behaviors.same
    }
}
