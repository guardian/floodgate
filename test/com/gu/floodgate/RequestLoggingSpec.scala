package com.gu.floodgate

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger => LogbackLogger}
import ch.qos.logback.core.read.ListAppender
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.LoggerFactory
import play.api.Application
import play.api.test.{FakeRequest, RouteInvokers, Writeables}
import support.{NonBrowserWithApplicationLoader => WithApplicationLoader}
import support.NonBrowserHelpers._

import scala.jdk.CollectionConverters._

class RequestLoggingSpec extends AnyFlatSpec with Matchers {

  private object Routing extends RouteInvokers with Writeables

  private class CapturedLogs {
    private val logger = LoggerFactory.getLogger(classOf[requestlogging.RequestLoggingFilter]).asInstanceOf[LogbackLogger]
    private val appender = new ListAppender[ILoggingEvent]
    appender.start()
    logger.addAppender(appender)

    def events: Seq[ILoggingEvent] = appender.list.asScala.toSeq
    def stop(): Unit = logger.detachAppender(appender)
  }

  private def withCapturedLogs[T](body: CapturedLogs => T): T = {
    val logs = new CapturedLogs
    try body(logs)
    finally logs.stop()
  }

  private def get(app: Application, path: String, headers: (String, String)*) = {
    val request = FakeRequest(GET, path).withHeaders(headers: _*).withBody("")
    status(Routing.route(app, request).get)
  }

  private def markerFields(event: ILoggingEvent): String = String.valueOf(event.getMarkerList)

  it should "log a structured entry containing the build commit id for a request" in new WithApplicationLoader(
    new AppLoader
  ) {
    withCapturedLogs { logs =>
      val responseStatus = get(app, "/no-such-route", "X-Forwarded-For" -> "203.0.113.7", "User-Agent" -> "request-logging-spec")

      val events = logs.events
      events should have size 1

      val event = events.head
      event.getFormattedMessage shouldBe "HTTP request received"

      val fields = markerFields(event)
      fields should include(s"build_commit=${_root_.app.BuildInfo.gitCommitId}")
      fields should include("http_method=GET")
      fields should include(s"http_status=$responseStatus")
      fields should include("origin=203.0.113.7")
      fields should include("user_agent=request-logging-spec")
      fields should include("route=<unmatched>")
      fields should include("duration_ms=")
      fields should include("request_id=")
    }
  }

  it should "log the matched route pattern and status for a request that hits a real route" in new WithApplicationLoader(
    new AppLoader
  ) {
    withCapturedLogs { logs =>
      get(app, "/metrics") should be(OK)

      val events = logs.events
      events should have size 1

      val fields = markerFields(events.head)
      fields should include("route=/metrics")
      fields should include(s"http_status=$OK")
      fields should not include "route=<unmatched>"
    }
  }

  it should "not log requests to the excluded healthcheck path" in new WithApplicationLoader(new AppLoader) {
    withCapturedLogs { logs =>
      get(app, "/healthcheck") should be(OK)

      logs.events shouldBe empty
    }
  }
}
