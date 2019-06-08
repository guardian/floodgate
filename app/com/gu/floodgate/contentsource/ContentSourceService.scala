package com.gu.floodgate.contentsource

import com.gu.floodgate.{ContentSourceNotFound, CustomError, DynamoDBTable}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.syntax.either._

class ContentSourceService(contentSourceTable: DynamoDBTable[ContentSource]) {

  def getAllContentSources(): Future[Seq[ContentSource]] = contentSourceTable.getAll()

  def getContentSources(id: String): Future[Either[CustomError, Seq[ContentSource]]] = {
    contentSourceTable.getItems(id) map { contentSources =>
      if (contentSources.isEmpty)
        Left(ContentSourceNotFound(s"A content source with id: $id does not exist."))
      else Right(contentSources)
    }
  }

  def getContentSource(id: String, environment: String): Either[CustomError, ContentSource] = {
    contentSourceTable.getItem(id, environment) match {
      case Some(result) =>
        result.leftMap(err => ContentSourceNotFound(s"A content source with id: $id does not exist."))
      case None => Left(ContentSourceNotFound(s"A content source with id: $id does not exist."))
    }
  }

  def createContentSource(contentSource: ContentSource) = contentSourceTable.saveItem(contentSource)
  def updateContentSource(id: String, environment: String, contentSource: ContentSource) =
    contentSourceTable.saveItem(contentSource)
  def deleteContentSource(id: String) = contentSourceTable.deleteItem(id)

}
