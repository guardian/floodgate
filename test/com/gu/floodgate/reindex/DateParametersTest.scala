package com.gu.floodgate.reindex

import com.gu.floodgate.CustomError
import org.joda.time.DateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.syntax.either._

class DateParametersTest extends AnyFlatSpec with Matchers {

  behavior of "DateParameters"

  it should "Correctly construct a DateParameters object with valid date parameters" in {

    val from = Some("2016-01-01T00:00:00Z")
    val to = Some("2016-01-31T00:00:00Z")

    val dateParametersOrError: Either[CustomError, DateParameters] = DateParameters(from, to)

    dateParametersOrError foreach { dp =>
      dp.from should be(Some(new DateTime(from.get)))
      dp.to should be(Some(new DateTime(to.get)))
    }

  }

  it should "Correctly construct a DateParameters object when date parameters are none" in {

    val from = None
    val to = None

    val dateParametersOrError: Either[CustomError, DateParameters] = DateParameters(from, to)

    dateParametersOrError foreach { dp =>
      dp.from should be(None)
      dp.to should be(None)
    }

  }

  it should "Produce an error when from date is invalid" in {

    val from = Some("45467")
    val to = Some("2016-01-31T00:00:00Z")

    val dateParametersOrError: Either[CustomError, DateParameters] = DateParameters(from, to)

    dateParametersOrError.leftMap(f => f.message should be("Date parameter provided is invalid."))

  }

  it should "Produce an error when to date is invalid" in {

    val from = Some("2016-01-01T00:00:00Z")
    val to = Some("INVALID-DATE")

    val dateParametersOrError: Either[CustomError, DateParameters] = DateParameters(from, to)

    dateParametersOrError.leftMap(f => f.message should be("Date parameter provided is invalid."))

  }
}
