package com.gu.floodgate.reindex

import com.gu.floodgate.{CustomError, InvalidDateTimeParameter}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import scala.util.{Success, Try}

case class DateParameters(from: Option[DateTime], to: Option[DateTime])

object DateParameters {

  private val formatter: DateTimeFormatter = ISODateTimeFormat.dateTime()

  def apply(from: Option[String], to: Option[String]): Either[CustomError, DateParameters] = {
    val fromDate = Try(from map parseDateTime)
    val toDate = Try(to map parseDateTime)

    (fromDate, toDate) match {
      case (Success(f), Success(t)) => Right(DateParameters(f, t))
      case _                        => Left(InvalidDateTimeParameter(s"Date parameter provided is invalid."))
    }
  }

  private def parseDateTime(dateTime: String) = formatter.parseDateTime(dateTime)
}
