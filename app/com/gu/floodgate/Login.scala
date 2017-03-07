package controllers

import play.api.mvc._
import com.gu.googleauth._
import play.api.libs.ws.WSClient
import play.api.Configuration
import com.gu.floodgate.views

trait AuthActions extends Actions with Filters {
  def conf: Configuration
  def groupChecker: GoogleGroupChecker = ???

  // Google configuration
  override val authConfig = GoogleAuthConfig(
    clientId = conf.getString("google.clientid").getOrElse(sys.error("No google clientid.")),
    clientSecret = conf.getString("google.clientsecret").getOrElse(sys.error("No google clientsecret.")),
    redirectUrl = conf.getString("google.oauthcallback").getOrElse(sys.error("No google oauthcallback.")),
    domain = "guardian.co.uk"
  )
  // your app's routing
  override val loginTarget = routes.Login.loginAction()
  override val defaultRedirectTarget = routes.Application.index()
  override val failureRedirectTarget = routes.Login.login()
}

class Login(val wsClient: WSClient, val conf: Configuration) extends Controller with AuthActions {

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
    Redirect(routes.Application.index()).withNewSession
  }
}
