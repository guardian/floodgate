package com.gu.floodgate.runningjob

import com.gu.floodgate.reindex.DateParameters
import org.joda.time.DateTime

case class RunningJob(
    contentSourceId: String,
    contentSourceEnvironment: String,
    documentsIndexed: Int,
    documentsExpected: Int,
    startTime: DateTime,
    rangeFrom: Option[DateTime],
    rangeTo: Option[DateTime]
)

object RunningJob {
  def apply(contentSourceId: String, contentSourceEnvironment: String, dateParameters: DateParameters): RunningJob =
    RunningJob(contentSourceId, contentSourceEnvironment, 0, 0, new DateTime(), dateParameters.from, dateParameters.to)
}

case class RunningJobsResponse(runningJobs: Seq[RunningJob])

case class SingleRunningJobResponse(runningJob: RunningJob)
