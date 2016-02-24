package com.gu.floodgate.runningjob

import org.joda.time.DateTime
import play.json.extra.JsonFormat

@JsonFormat
case class RunningJob(contentSourceId: String, contentSourceEnvironment: String, documentsIndexed: Int, documentsExpected: Int, startTime: DateTime)

object RunningJob {
  def apply(contentSourceId: String, contentSourceEnvironment: String): RunningJob = RunningJob(contentSourceId, contentSourceEnvironment, 0, 0, new DateTime())
}

@JsonFormat
case class RunningJobsResponse(runningJobs: Seq[RunningJob])

@JsonFormat
case class SingleRunningJobResponse(runningJob: RunningJob)
