package com.gu.floodgate.contentsource

import com.gu.floodgate.DynamoDBTable

import scala.concurrent.Future

class ContentSourceService(contentSourceTable: DynamoDBTable[ContentSource]) {

  def getContentSources(): Future[Seq[ContentSource]] = contentSourceTable.getAll()
  def getContentSource(id: String): Future[Option[ContentSource]] = contentSourceTable.getItem(id)
  def createContentSource(contentSource: ContentSource) = contentSourceTable.saveItem(contentSource)
  def updateContentSource(id: String, contentSource: ContentSource) = contentSourceTable.updateItem(id, contentSource)
  def deleteContentSource(id: String) = contentSourceTable.deleteItem(id)

}
