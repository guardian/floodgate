package com.gu.floodgate

import play.api.Play
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import scala.concurrent.Future
import com.gu.googleauth._
import play.api.libs.ws.WSAPI
import play.api.Play.current
import org.joda.time.Duration

trait AuthActions extends Actions {
  def loginTarget: Call = routes.Login.login()
  def googleAuthConfig =
    GoogleAuthConfig(
      Play.configuration.getString("google.clientid").getOrElse(sys.error("No google clientid.")),
      Play.configuration.getString("google.clientsecret").getOrElse(sys.error("No google clientsecret.")),
      Play.configuration.getString("google.oauthcallback").getOrElse(sys.error("No google oauthcallback.")),
      Some("guardian.co.uk"),
      Some(Duration.standardDays(30)),
      true
    )
  def authConfig = googleAuthConfig
}

class Login(ws: WSAPI) extends Controller with AuthActions {
  val ANTI_FORGERY_KEY = "antiForgeryToken"

  def login = Action.async { implicit request =>
    val antiForgeryToken = GoogleAuth.generateAntiForgeryToken()
    GoogleAuth.redirectToGoogle(googleAuthConfig, antiForgeryToken, ws).map {
      _.withSession { request.session + (ANTI_FORGERY_KEY -> antiForgeryToken) }
    }
  }

  def oauth2Callback = Action.async { implicit request =>
    val session = request.session
    session.get(ANTI_FORGERY_KEY) match {
      case None =>
        Future.successful(Redirect(routes.Login.login()).flashing("error" -> "Anti forgery token missing in session"))
      case Some(token) =>
        GoogleAuth.validatedUserIdentity(googleAuthConfig, token, ws).map { identity =>

          val redirect = session.get(LOGIN_ORIGIN_KEY) match {
            case Some(url) => Redirect(url)
            case None => InternalServerError
          }
          redirect.withSession {
            session + (UserIdentity.KEY -> Json.toJson(identity).toString) - ANTI_FORGERY_KEY - LOGIN_ORIGIN_KEY
          }
        } recover {
          case t =>
            Redirect(routes.Login.login())
              .withSession(session - ANTI_FORGERY_KEY)
              .flashing("error" -> s"Login failure: ${t.toString}")
        }
    }
  }

}
