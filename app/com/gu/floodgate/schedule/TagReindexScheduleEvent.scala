package com.gu.floodgate.schedule

import com.gu.floodgate.contentsource.ContentSourceService
import com.gu.floodgate.reindex.{DateParameters, ReindexService}
import com.typesafe.scalalogging.StrictLogging
import org.apache.pekko.actor.typed.scaladsl.Behaviors.same
import org.apache.pekko.actor.typed.{Behavior, PreRestart}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

class TagReindexScheduleEvent(contentSourceService: ContentSourceService, reindexService: ReindexService, val environment: String)(implicit ec: ExecutionContext) extends ScheduleEvent with StrictLogging {
  val appName = "Tag Manager"
  val appLogId = s"app name: '$appName', env: '$environment'"

  def behavior: Behavior[ReindexMessage] = Behaviors.setup { context =>
    logger.info(s"Starting actor for $appLogId")
    Behaviors.receiveMessage[ReindexMessage] {
      case ScheduledReindexRequest() =>
        val result = contentSourceService.getContentSourceByName(appName, environment) flatMap {
          case Some(contentSource) =>
            reindexService.reindex(contentSource.id, environment, DateParameters(None, None)).map {
              case Right(runningJob) =>
                logger.info(s"Running scheduled reindex for service $appLogId with job number ${runningJob.contentSourceId}")
                Right(())
              case Left(customError) =>
                logger.warn(
                  s"Attempted to run scheduled reindex for service $appLogId, but received error: ${customError.message}",
                )

                Left(customError.message)
            }
          case None =>
            val message = s"Attempted to run scheduled reindex for service $appLogId, but could not find a content source for that configuration"
            logger.warn(message)
            Future.successful(Left(message))
        }

        result.collect {
          case Left(message) =>
            // Gather asynchronous errors here, and pass back to the actor to handle
            context.self ! ScheduledReindexError(message)
        }

        same[ReindexMessage]
      case ScheduledReindexError(message) =>
        // Let the actor crash, the supervisor will apply the necessary strategy
        throw new Error(message)
    }
  }
}
