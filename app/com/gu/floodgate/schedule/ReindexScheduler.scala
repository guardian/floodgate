package com.gu.floodgate.schedule

import com.gu.floodgate.schedule.ReindexSchedule.ScheduledReindexEvent
import org.apache.pekko.actor.typed.Behavior

trait ReindexScheduler {
  val id: String
  val environment: String
  def behavior: Behavior[ScheduledReindexEvent]
}
