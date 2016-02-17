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
  protected val keyName: String
  protected def fromItem(item: Map[String, AttributeValue]): T
  protected def toItem(t: T): Map[String, AttributeValue]
  protected def toItemUpdate(t: T): Map[String, AttributeValueUpdate]

  def getAll(): Future[Seq[T]] = {
    val request = new ScanRequest().withTableName(tableName)
    val promise = Promise[List[T]]()

    val responseHandler = new AsyncHandler[ScanRequest, ScanResult] {

      override def onError(e: Exception) = {
        logger.error(e.getMessage)
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

  def getItem(id: String, keyName: String = "id"): Future[Option[T]] = {
    val promise = Promise[Option[T]]()

    val request = new QueryRequest().withTableName(tableName)
      .withKeyConditionExpression(s"$keyName = :$keyName")
      .withExpressionAttributeValues(Map(s":$keyName" -> new AttributeValue().withS(id)))

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

  def getItems(id: String) = {
    val promise = Promise[List[T]]()

    val request = new QueryRequest().withTableName(tableName)
      .withScanIndexForward(false)
      .withKeyConditionExpression(s"$keyName = :$keyName")
      .withExpressionAttributeValues(
        Map(s":$keyName" -> new AttributeValue().withS(id)))

    val responseHandler = new AsyncHandler[QueryRequest, QueryResult] {

      override def onError(e: Exception) = {
        logger.warn(s"Could not retrieve item with hash key: $id: ${e.getMessage} ")
        promise.failure(e)
      }

      override def onSuccess(request: QueryRequest, result: QueryResult) = {
        val item: List[T] = result.getItems.asScala.toList.map(f => fromItem(f.toMap))
        promise.success(item)
      }

    }

    dynamoDB.queryAsync(request, responseHandler)
    promise.future
  }

  def saveItem(t: T): Future[T] = {
    val request = new PutItemRequest().withTableName(tableName).withItem(toItem(t).asJava)
    val promise = Promise[T]()
    val responseHandler = new AsyncHandler[PutItemRequest, PutItemResult] {

      override def onError(e: Exception) = {
        logger.error(e.getMessage)
        promise.failure(e)
      }

      override def onSuccess(request: PutItemRequest, result: PutItemResult) = {
        promise.success(t)
      }

    }

    dynamoDB.putItemAsync(request, responseHandler)
    promise.future
  }

  def updateItem(id: String, t: T): Unit = {
    val request = new UpdateItemRequest().withTableName(tableName)
      .withKey(Map(keyName -> new AttributeValue(id)))
      .withAttributeUpdates(toItemUpdate(t).mapValues(_.withAction("PUT")))

    dynamoDB.updateItemAsync(request)
  }

  def deleteItem(id: String): Future[DeleteItemResult] = {
    val request = new DeleteItemRequest().withTableName(tableName).withKey(Map(keyName -> new AttributeValue(id)))
    val promise = Promise[DeleteItemResult]()
    val responseHandler = new AsyncHandler[DeleteItemRequest, DeleteItemResult] {

      override def onError(e: Exception) = {
        logger.error(e.getMessage)
        promise.failure(e)
      }

      override def onSuccess(request: DeleteItemRequest, result: DeleteItemResult) = {
        promise.success(result)
      }

    }

    dynamoDB.deleteItemAsync(request, responseHandler)
    promise.future
  }

  def getItemAttributeValue(key: String, item: Map[String, AttributeValue]): AttributeValue = {
    item.getOrElse(key, {
      logger.warn(s"Provided key $key has no value.")
      new AttributeValue("")
    })
  }

}
