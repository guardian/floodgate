package com.gu.floodgate.jobhistory

import com.gu.floodgate.reindex.{DateParameters, ReindexStatus}
import org.joda.time.DateTime

case class JobHistory(
    contentSourceId: String,
    startTime: DateTime,
    finishTime: DateTime,
    status: ReindexStatus,
    environment: String,
    rangeFrom: Option[DateTime],
    rangeTo: Option[DateTime],
    documentsExpected: Option[Int],
    documentsIndexed: Option[Int]
)

case class JobHistoriesResponse(jobHistories: Seq[JobHistory])
