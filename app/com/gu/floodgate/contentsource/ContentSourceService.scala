package com.gu.floodgate.contentsource

import com.gu.floodgate.{ ContentSourceNotFound, CustomError, DynamoDBTable }
import org.scalactic.{ Bad, Good, Or }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ContentSourceService(contentSourceTable: DynamoDBTable[ContentSource]) {

  def getAllContentSources(): Future[Seq[ContentSource]] = contentSourceTable.getAll()

  def getContentSources(id: String): Future[Seq[ContentSource] Or CustomError] = {
    contentSourceTable.getItems(id) map { contentSources =>
      if (contentSources.isEmpty)
        Bad(ContentSourceNotFound(s"A content source with id: $id does not exist."))
      else Good(contentSources)
    }
  }

  def getContentSource(id: String, environment: String): ContentSource Or CustomError = {
    Or.from(
      option = contentSourceTable.getItem[ContentSource](id, environment).flatMap(_.toOption),
      orElse = ContentSourceNotFound(s"A content source with id: $id does not exist.")
    )
  }

  def createContentSource(contentSource: ContentSource) = contentSourceTable.saveItem(contentSource)
  def updateContentSource(id: String, environment: String, contentSource: ContentSource) = contentSourceTable.updateItem(id, environment, contentSource)
  def deleteContentSource(id: String) = contentSourceTable.deleteItem[ContentSource](id)

}
