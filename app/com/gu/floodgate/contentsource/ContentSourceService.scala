package com.gu.floodgate.contentsource

import scala.concurrent.Future

class ContentSourceService {

  def getContentSources(): Future[List[ContentSource]] = ???
  def getContentSource(id: String): Future[Option[ContentSource]] = ???
  def createContentSource(contentSource: ContentSource) = ???
  def updateContentSource(contentSource: ContentSource) = ???
  def deleteContentSource(id: String) = ???
}
