package com.gu.floodgate.contentsource

import com.gu.floodgate.{ ContentSourceNotFound, CustomError, DynamoDBTable }
import org.scalactic.Or
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ContentSourceService(contentSourceTable: DynamoDBTable[ContentSource]) {

  def getContentSources(): Future[Seq[ContentSource]] = contentSourceTable.getAll()

  def getContentSource(id: String): Future[ContentSource Or CustomError] = {
    contentSourceTable.getItem(id) map { maybeContentSource =>
      Or.from(
        option = maybeContentSource,
        orElse = ContentSourceNotFound(s"A content source with id: $id does not exist.")
      )
    }
  }

  def createContentSource(contentSource: ContentSource) = contentSourceTable.saveItem(contentSource)
  def updateContentSource(id: String, contentSource: ContentSource) = contentSourceTable.updateItem(id, contentSource)
  def deleteContentSource(id: String) = contentSourceTable.deleteItem(id)

}
