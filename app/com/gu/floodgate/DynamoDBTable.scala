package com.gu.floodgate

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.handlers.AsyncHandler
import com.typesafe.scalalogging.StrictLogging
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.{ Promise, Future }

trait DynamoDBTable[T] extends StrictLogging {

  protected val dynamoDB: AmazonDynamoDBAsync
  protected val tableName: String
  protected def fromItem(item: Map[String, AttributeValue]): T
  protected def toItem(t: T): Map[String, AttributeValue]
  protected def toItemUpdate(t: T): Map[String, AttributeValueUpdate]

  def getAll(): Future[Seq[T]] = {
    val request = new ScanRequest().withTableName(tableName)
    val promise = Promise[List[T]]()

    val responseHandler = new AsyncHandler[ScanRequest, ScanResult] {

      override def onError(e: Exception) = {
        promise.failure(e)
      }

      override def onSuccess(request: ScanRequest, result: ScanResult) = {
        val items: List[T] = result.getItems.asScala.toList.map(f => fromItem(f.toMap))
        promise.success(items)
      }

    }

    dynamoDB.scanAsync(request, responseHandler)
    promise.future
  }

  def getItem(id: String): Future[Option[T]] = {
    val promise = Promise[Option[T]]()

    val request = new QueryRequest().withTableName(tableName)
      .withKeyConditionExpression("id = :id")
      .withExpressionAttributeValues(Map(":id" -> new AttributeValue().withS(id)))

    val responseHandler = new AsyncHandler[QueryRequest, QueryResult] {

      override def onError(e: Exception) = {
        logger.warn(s"Could not retrieve item with ID: $id: ${e.getMessage}")
        promise.failure(e)
      }

      override def onSuccess(request: QueryRequest, result: QueryResult) = {
        val item: Option[T] = result.getItems.asScala.headOption.map(f => fromItem(f.toMap))
        promise.success(item)
      }

    }

    dynamoDB.queryAsync(request, responseHandler)
    promise.future
  }

  def saveItem(t: T): Unit = {
    dynamoDB.putItemAsync {
      new PutItemRequest().withTableName(tableName).withItem(toItem(t).asJava)
    }
  }

  def updateItem(id: String, t: T): Unit = {
    val request = new UpdateItemRequest().withTableName(tableName)
      .withKey(Map("id" -> new AttributeValue(id)))
      .withAttributeUpdates(toItemUpdate(t).mapValues(_.withAction("update")))

    dynamoDB.updateItemAsync(request)
  }

  def deleteItem(id: String) = {
    val request = new DeleteItemRequest().withTableName(tableName).withKey(Map("id" -> new AttributeValue(id)))
    dynamoDB.deleteItemAsync(request)
  }

  def getItemAttributeValue(key: String, item: Map[String, AttributeValue]): AttributeValue = {
    item.get(key).getOrElse {
      logger.warn(s"Provided key $key has no value.")
      new AttributeValue("")
    }
  }

}
