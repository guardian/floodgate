package com.gu.floodgate.reindex

sealed trait ReindexStatus

object ReindexStatus {

  def asString(s: ReindexStatus): String = s match {
    case InProgress => "in progress"
    case Failed     => "failed"
    case Completed  => "completed"
    case Cancelled  => "cancelled"
    case Unknown    => "unknown"
  }

  def fromString(s: String): Option[ReindexStatus] = s.toLowerCase match {
    case "in progress" => Some(InProgress)
    case "failed"      => Some(Failed)
    case "completed"   => Some(Completed)
    case "cancelled"   => Some(Cancelled)
    case "unknown"     => Some(Unknown)
    case _             => None
  }

}

case object InProgress extends ReindexStatus
case object Failed extends ReindexStatus
case object Completed extends ReindexStatus
case object Cancelled extends ReindexStatus
case object Unknown extends ReindexStatus

case class Progress(status: ReindexStatus, documentsIndexed: Int, documentsExpected: Int)
