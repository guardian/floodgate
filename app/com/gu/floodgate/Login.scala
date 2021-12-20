package controllers

import com.gu.googleauth._
import play.api.libs.ws.WSClient
import play.api.Configuration
import com.gu.floodgate.views
import play.api.mvc.Call
import play.api.mvc.legacy.Controller

import scala.concurrent.ExecutionContext

class Login(val authConfig: GoogleAuthConfig, val wsClient: WSClient, val conf: Configuration)(
    implicit executionContext: ExecutionContext
) extends Controller
    with LoginSupport {

  /**
    * Shows UI for login button and logout error feedback
    */
  def login = Action { request =>
    val error = request.flash.get("error")
    Ok(views.html.login(error))
  }

  /*
   * Redirect to Google with anti forgery token (that we keep in session storage - note that flashing is NOT secure).
   */
  def loginAction = Action.async { implicit request =>
    startGoogleLogin()
  }

  /*
   * Looks up user's identity via Google and (optionally) enforces required Google groups at login time.
   *
   * To re-check Google group membership on every page request you can use the `requireGroup` filter
   * (see `Application.scala`).
   */
  def oauth2Callback = Action.async { implicit request =>
    processOauth2Callback()
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.index).withNewSession
  }

  override val failureRedirectTarget: Call = routes.Login.login
  override val defaultRedirectTarget: Call = routes.Application.index
}
