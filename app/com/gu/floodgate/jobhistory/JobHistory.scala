package com.gu.floodgate.jobhistory

import com.gu.floodgate.reindex.{ DateParameters, ReindexStatus }
import org.joda.time.DateTime
import play.json.extra.JsonFormat

@JsonFormat
case class JobHistory(contentSourceId: String, startTime: DateTime, finishTime: DateTime, status: ReindexStatus, environment: String, rangeFrom: Option[DateTime], rangeTo: Option[DateTime])

@JsonFormat
case class JobHistoriesResponse(jobHistories: Seq[JobHistory])