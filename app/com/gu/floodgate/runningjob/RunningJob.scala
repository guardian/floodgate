package com.gu.floodgate.runningjob

import org.joda.time.DateTime
import play.json.extra.JsonFormat

@JsonFormat
case class RunningJob(contentSourceId: String, progress: Int, startTime: DateTime)

object RunningJob {
  def apply(contentSourceId: String): RunningJob = RunningJob(contentSourceId, 0, new DateTime())
}

@JsonFormat
case class RunningJobsResponse(runningJobs: Seq[RunningJob])

@JsonFormat
case class SingleRunningJobResponse(runningJob: RunningJob)
