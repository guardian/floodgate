package com.gu.floodgate.jobhistory

import org.joda.time.DateTime
import play.json.extra.JsonFormat

// TODO make status enum once we have better idea of statuses to represent.
@JsonFormat
case class JobHistory(id: String, status: String, startTime: DateTime, finishTime: DateTime)

@JsonFormat
case class JobHistoriesResponse(jobHistories: Seq[JobHistory])

@JsonFormat
case class SingleJobHistoryResponse(jonHistory: JobHistory)