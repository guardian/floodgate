package controllers

import akka.util.ByteString
import com.gu.floodgate.reindex.{Completed, InProgress, Progress}
import com.typesafe.scalalogging.StrictLogging
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.legacy.Controller
import com.gu.floodgate.views
import play.api.libs.ws.WSClient
import com.gu.floodgate.Formats._
import com.gu.googleauth.{AuthAction, GoogleAuthConfig}
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import play.api.http.HttpEntity
import play.api.mvc.{AnyContent, ResponseHeader, Result}

import java.io.StringWriter

class Application(
    authAction: AuthAction[AnyContent],
    authConfig: GoogleAuthConfig,
    val wsClient: WSClient,
    val conf: Configuration
) extends Controller
    with StrictLogging {

  def index = authAction {
    Ok(views.html.app("Floodgate"))
  }

  def metrics = Action {
    val writer = new StringWriter()
    TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())
    Result(
      ResponseHeader(200),
      HttpEntity.Strict(ByteString(writer.toString), Some("text/plain; version=0.0.4; charset=UTF-8")))
  }

  /*
   * The below endpoints are provided to demonstrate the expected behavior of the endpoints implemented
   * on a content source. Tests against these, for the benefit of anyone implementing them in a new content source,
   * are provided in order to aid them.
   *
   */

  def fakeReindexRouteInitiate = Action { Ok("") }
  def fakeReindexRouteInitiateButInProgress = Action { Forbidden }
  def fakeReindexRouteCancel = Action { Ok("") }

  var progress = 0
  def fakeReindexRouteProgress = Action { implicit request =>
    println(s"Reindex progress.")
    progress = progress + 10
    if (progress == 100) {
      progress = 0
      Ok(Json.toJson(Progress(Completed, 100, 100)))
    } else {
      Ok(Json.toJson(Progress(InProgress, progress, 100)))
    }
  }

  def fakeReindexRouteProgressShowingCompleted = Action { Ok(Json.toJson(Progress(Completed, 100, 100))) }
  def fakeReindexRouteProgressButNeverRunBefore = Action { NotFound }

}
