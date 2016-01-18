package com.gu.floodgate.runningjob

import org.joda.time.DateTime
import play.json.extra.JsonFormat

@JsonFormat
case class RunningJob(id: String, progress: Double, startTime: DateTime)

@JsonFormat
case class RunningJobsResponse(runningJobs: Seq[RunningJob])

@JsonFormat
case class SingleRunningJobResponse(runningJob: RunningJob)
