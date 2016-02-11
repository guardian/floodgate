package com.gu.floodgate.jobhistory

import org.joda.time.DateTime
import play.json.extra.JsonFormat

// TODO make status enum once we have better idea of statuses to represent.
@JsonFormat
case class JobHistory(contentSourceId: String, startTime: DateTime, finishTime: DateTime, status: String)

@JsonFormat
case class JobHistoriesResponse(jobHistories: Seq[JobHistory])