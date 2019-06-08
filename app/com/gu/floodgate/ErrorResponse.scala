package com.gu.floodgate

import play.api.libs.json.Json

/* Type indicating an operation has been successful but does not necessarily need/require a return type */
case class Happy()

sealed trait CustomError {
  val message: String
}

case class InvalidDateTimeParameter(message: String) extends CustomError
case class ContentSourceNotFound(message: String) extends CustomError
case class ReindexAlreadyRunning(message: String) extends CustomError
case class ReindexCannotBeInitiated(message: String) extends CustomError
case class BulkReindexInProcess(message: String) extends CustomError
case class CancellingReindexFailed(message: String) extends CustomError
case class RunningJobNotFound(message: String) extends CustomError
case class ScanamoReadError(message: String) extends CustomError

case class ErrorResponse(errorMessage: String)
