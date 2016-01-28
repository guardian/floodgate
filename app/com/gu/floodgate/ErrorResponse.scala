package com.gu.floodgate

import play.json.extra.JsonFormat

sealed trait CustomError {
  val message: String
}

case class ContentSourceNotFound(message: String) extends CustomError
case class ReindexAlreadyRunning(message: String) extends CustomError
case class ReindexCannotBeInitiated(message: String) extends CustomError
case class RunningJobNotFound(message: String) extends CustomError

@JsonFormat
case class ErrorResponse(errorMessage: String)
