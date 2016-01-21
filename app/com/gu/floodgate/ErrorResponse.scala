package com.gu.floodgate

import play.json.extra.JsonFormat

@JsonFormat
case class ErrorResponse(error: String)
