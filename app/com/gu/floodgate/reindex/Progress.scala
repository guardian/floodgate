package com.gu.floodgate.reindex

import play.json.extra.JsonFormat
import macrame.enum
import play.json.extra.Picklers._

@enum class ReindexStatus {
  InProgress("in progress")
  Failed("failed")
  Completed("completed")
  Cancelled("cancelled")
  Unknown("unknown")
}

object ReindexStatus extends EnumStringJSON[ReindexStatus] {
  def asString(s: ReindexStatus): String = asStringImpl(s)
  def fromString(s: String): Option[ReindexStatus] = fromStringImpl(s)
}

@JsonFormat
case class Progress(status: ReindexStatus, documentsIndexed: Int, documentsExpected: Int)
