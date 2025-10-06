package com.gu.floodgate.schedule

import com.gu.floodgate.contentsource.ContentSourceService
import com.gu.floodgate.reindex.{DateParameters, ReindexService}
import com.typesafe.scalalogging.StrictLogging
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import scala.concurrent.ExecutionContext

class TagReindexScheduleEvent(contentSourceService: ContentSourceService, reindexService: ReindexService, val environment: String)(implicit ec: ExecutionContext) extends ScheduleEvent with StrictLogging {
    val appName = "Tag Manager"

    def behavior: Behavior[ReindexSchedule.ScheduledReindexEvent] = Behaviors.receive { (_, _) =>
      contentSourceService.getContentSourceByName(appName, environment) map {
        case Some(contentSource) =>
          reindexService.reindex(contentSource.id, environment, DateParameters(None, None)).map {
            case Right(runningJob) =>
              logger.info(s"Running scheduled reindex for service $appName with job number ${runningJob.contentSourceId}")
            case Left(customError) =>
              logger.warn(
                s"Attempted to run scheduled reindex for service $appName, but received error: ${customError.message}",
              )
              // Let the actor crash, the supervisor will apply the necessary strategy
              throw new Error(customError.message)
          }
        case None =>
          val message = s"Attempted to run scheduled reindex for service $appName, but could not find a content source for that name and environment $environment"
          logger.warn(message)
          throw new Error(message)
      }

      Behaviors.same
    }
}
