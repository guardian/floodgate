package com.gu.floodgate.schedule

import org.apache.pekko.actor.typed.Behavior

trait ScheduleEvent {
  val appName: String
  val environment: String
  def behavior: Behavior[ReindexMessage]
}
