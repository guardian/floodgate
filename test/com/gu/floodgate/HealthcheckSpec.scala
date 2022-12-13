package controllers

import com.gu.floodgate.AppLoader
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.test.{FakeRequest, WithApplicationLoader}
import play.api.test.Helpers._

class HealthcheckSpec extends AnyFlatSpec with Matchers {

  val healthcheck = new Healthcheck
  val appLoader = new AppLoader

  it should "return a 200 if application is up and running when hitting healthcheck endpoint" in new WithApplicationLoader(
    appLoader
  ) {
    val resp = healthcheck.healthcheck.apply(FakeRequest())
    status(resp) should be(OK)
    contentAsString(resp) should be("ok")
  }

}
