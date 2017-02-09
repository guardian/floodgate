package com.gu.floodgate

import org.scalatest.{ FlatSpec, Matchers }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.Configuration
import play.api.Environment

class ApplicationSpec extends FlatSpec with Matchers {

  val configuration = Configuration.load(Environment.simple())
  val application = new Application(configuration)

  it should "return a 200 if application is up and running when hitting healthcheck endpoint" in {
    val resp = application.healthcheck.apply(FakeRequest())
    status(resp) should be(OK)
    contentAsString(resp) should be("ok")
  }

}
