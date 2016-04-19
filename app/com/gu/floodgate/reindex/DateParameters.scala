package com.gu.floodgate.reindex

import com.gu.floodgate.{ CustomError, InvalidDateTimeParameter }
import org.joda.time.DateTime
import org.joda.time.format.{ ISODateTimeFormat, DateTimeFormatter }
import org.scalactic.{ Or, Bad, Good }
import play.json.extra.JsonFormat

import scala.util.{ Success, Try }

@JsonFormat
case class DateParameters(from: Option[DateTime], to: Option[DateTime])

object DateParameters {

  private val formatter: DateTimeFormatter = ISODateTimeFormat.dateTime()

  def apply(from: Option[String], to: Option[String]): DateParameters Or CustomError = {
    val fromDate = Try(from map parseDateTime)
    val toDate = Try(to map parseDateTime)

    (fromDate, toDate) match {
      case (Success(f), Success(t)) => Good(DateParameters(f, t))
      case _ => Bad(InvalidDateTimeParameter(s"Date parameter provided is invalid."))
    }
  }

  private def parseDateTime(dateTime: String) = formatter.parseDateTime(dateTime)
}
