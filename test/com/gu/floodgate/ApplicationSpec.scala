package com.gu.floodgate

import com.gu.floodgate.Application
import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.iteratee.Concurrent
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ApplicationSpec extends FlatSpec with Matchers {

  val application = new Application

  it should "return a 200 if application is up and running when hitting healthcheck endpoint" in {
    val resp = application.healthcheck.apply(FakeRequest())
    status(resp) should be(OK)
    contentAsString(resp) should be("ok")
  }

}
