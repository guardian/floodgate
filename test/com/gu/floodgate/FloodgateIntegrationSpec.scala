package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.gu.floodgate.AppLoader
import com.gu.floodgate.reindex.{Completed, InProgress, Progress}
import org.scalatest.{FlatSpec, Matchers}
import play.api.mvc.Call
import play.api.test.{FakeRequest, WithApplicationLoader}
import play.api.test.Helpers._
import play.api.Configuration
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSClient
import com.gu.floodgate.Formats._

/**
  * These tests are provided as a way of demonstrating how we expect the reindex endpoints on a particular content
  * source to behave in order to have integration with Floodgate.
  */
class FloodgateIntegrationSpec extends FlatSpec with Matchers {

  val reindexRoute = "/reindex"
  val initiateReindexRequest = new Call("POST", reindexRoute)
  val cancelReindexRequest = new Call("DELETE", reindexRoute)
  val progressReindexRequest = new Call("GET", reindexRoute)

  val appLoader = new AppLoader

  behavior of "initiating a reindex"

  it should "return a 200 or 201 when a reindex is initiated" in new WithApplicationLoader(appLoader) {
    val application = appLoader.c.appController
    val resp = application.fakeReindexRouteInitiate.apply(FakeRequest(initiateReindexRequest))
    status(resp) should (be(OK) or be(CREATED))
  }

  it should "return a 403 (forbidden) when a reindex is initiated but one is already in progress" in new WithApplicationLoader(
    appLoader
  ) {
    val application = appLoader.c.appController
    val resp = application.fakeReindexRouteInitiateButInProgress.apply(FakeRequest(initiateReindexRequest))
    status(resp) should be(FORBIDDEN)
  }

  behavior of "cancelling a reindex"

  it should "return a 200 when a reindex is cancelled " in new WithApplicationLoader(appLoader) {
    val application = appLoader.c.appController
    val resp = application.fakeReindexRouteCancel.apply(FakeRequest(cancelReindexRequest))
    status(resp) should be(OK)
  }

  behavior of "retrieving progress for a reindex"

  it should "return a 404 if there has never been a reindex initiated before" in new WithApplicationLoader(appLoader) {
    val application = appLoader.c.appController
    val resp = application.fakeReindexRouteProgressButNeverRunBefore.apply(FakeRequest(progressReindexRequest))
    status(resp) should be(NOT_FOUND)
  }

  it should "return a 200 and a progress update when asked for progress and there is a reindex running" in new WithApplicationLoader(
    appLoader
  ) {
    val application = appLoader.c.appController
    val resp = application.fakeReindexRouteProgress.apply(FakeRequest(progressReindexRequest))
    status(resp) should be(OK)
    contentAsJson(resp).validate[Progress].map { progress =>
      progress should be(Progress(InProgress, 10, 100))
    }
  }

  it should "return a 200 and a progress update showing progress completed or failed when asked for progress" +
    "and the last running reindex has just finished. This should be shown until such a time when another reindex is initiated." in new WithApplicationLoader(
    appLoader
  ) {
    val application = appLoader.c.appController
    val resp = application.fakeReindexRouteProgressShowingCompleted.apply(FakeRequest(progressReindexRequest))
    status(resp) should be(OK)
    contentAsJson(resp).validate[Progress].map { progress =>
      progress should be(Progress(Completed, 100, 100))
    }
  }

}
