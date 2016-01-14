package com.gu.floodgate.contentsource

import play.api.mvc.{ Action, Controller }

import scala.concurrent.Future

class ContentSourceApi(contentSourceService: ContentSourceService) extends Controller {

  def getContentSources = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

  def getContentSource(id: String) = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

  def createContentSource = Action.async(parse.json) { implicit request =>
    Future.successful(Created(""))
  }

  def updateContentSource(id: String) = Action.async(parse.json) { implicit request =>
    Future.successful(Ok(""))
  }

  def deleteContentSource(id: String) = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

}
