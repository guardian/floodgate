package com.gu.floodgate

import com.gu.floodgate.reindex.Progress
import org.scalatest.{ FlatSpec, Matchers }
import play.api.mvc.{ Call }
import com.gu.floodgate.reindex.ReindexStatus.{ InProgress, Completed }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.Configuration
import play.api.Environment

/**
 * These tests are provided as a way of demonstating how we expect the reindex endpoints on a particular content
 * source to behave in order to have integration with Floodgate.
 */
class FloodgateIntegrationSpec extends FlatSpec with Matchers {

  val configuration = Configuration.load(Environment.simple())
  val application = new Application(configuration)

  val reindexRoute = "/reindex"
  val initiateReindexRequest = new Call("POST", reindexRoute)
  val cancelReindexRequest = new Call("DELETE", reindexRoute)
  val progressReindexRequest = new Call("GET", reindexRoute)

  behavior of "initiating a reindex"

  it should "return a 200 or 201 when a reindex is initiated" in {
    val resp = application.fakeReindexRouteInitiate.apply(FakeRequest(initiateReindexRequest))
    status(resp) should (be(OK) or be(CREATED))
  }

  it should "return a 403 (forbidden) when a reindex is initiated but one is already in progress" in {
    val resp = application.fakeReindexRouteInitiateButInProgress.apply(FakeRequest(initiateReindexRequest))
    status(resp) should be(FORBIDDEN)
  }

  behavior of "cancelling a reindex"

  it should "return a 200 when a reindex is cancelled " in {
    val resp = application.fakeReindexRouteCancel.apply(FakeRequest(cancelReindexRequest))
    status(resp) should be(OK)
  }

  behavior of "retrieving progress for a reindex"

  it should "return a 404 if there has never been a reindex initiated before" in {
    val resp = application.fakeReindexRouteProgressButNeverRunBefore.apply(FakeRequest(progressReindexRequest))
    status(resp) should be(NOT_FOUND)
  }

  it should "return a 200 and a progress update when asked for progress and there is a reindex running" in {
    val resp = application.fakeReindexRouteProgress.apply(FakeRequest(progressReindexRequest))
    status(resp) should be(OK)
    contentAsJson(resp).validate[Progress].map { progress =>
      progress should be(Progress(InProgress, 10, 100))
    }
  }

  it should "return a 200 and a progress update showing progress completed or failed when asked for progress" +
    "and the last running reindex has just finished. This should be shown until such a time when another reindex is initiated." in {
      val resp = application.fakeReindexRouteProgressShowingCompleted.apply(FakeRequest(progressReindexRequest))
      status(resp) should be(OK)
      contentAsJson(resp).validate[Progress].map { progress =>
        progress should be(Progress(Completed, 100, 100))
      }
    }

}

