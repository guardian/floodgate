package com.gu.floodgate

import cats.data.ValidatedNel
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.handlers.AsyncHandler
import com.gu.scanamo.{ DynamoReadError, DynamoFormat, Scanamo }
import com.typesafe.scalalogging.StrictLogging
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.{ Promise, Future }

trait DynamoDBTable[T] extends StrictLogging {

  protected val dynamoDB: AmazonDynamoDBAsync
  protected val tableName: String
  protected val keyName: String
  protected val maybeSortKeyName: Option[String] = None
  protected def fromItem(item: Map[String, AttributeValue]): T
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

  def getItem[T: DynamoFormat](hashKey: String, sortKey: String): Option[ValidatedNel[DynamoReadError, T]] = {
    maybeSortKeyName flatMap { sortKeyName =>
      Scanamo.get[String, T](dynamoDB)(tableName)(keyName -> hashKey, sortKeyName -> sortKey)
    } orElse None
  }

  def getItems(id: String): Future[List[T]] = {
    val request = new QueryRequest().withTableName(tableName)
      .withScanIndexForward(false)
      .withKeyConditionExpression(s"$keyName = :$keyName")
      .withExpressionAttributeValues(Map(s":$keyName" -> new AttributeValue().withS(id)))

    getListOfItems(request)
  }

  def getItemsWithFilter(id: String, filterKeyName: String, filterValue: String): Future[List[T]] = {
    val request = new QueryRequest().withTableName(tableName)
      .withScanIndexForward(false)
      .withKeyConditionExpression(s"$keyName = :$keyName")
      .withFilterExpression(s"$filterKeyName = :$filterKeyName")
      .withExpressionAttributeValues(Map(
        s":$keyName" -> new AttributeValue().withS(id),
        s":$filterKeyName" -> new AttributeValue().withS(filterValue)
      ))

    getListOfItems(request)
  }

  def saveItem[T: DynamoFormat](t: T): PutItemResult = {
    Scanamo.put(dynamoDB)(tableName)(t)
  }

  def updateItem(id: String, t: T): Unit = {
    val request = new UpdateItemRequest().withTableName(tableName)
      .withKey(Map(keyName -> new AttributeValue(id)))
      .withAttributeUpdates(toItemUpdate(t).mapValues(_.withAction(AttributeAction.PUT)))

    updateThisItem(request)
  }

  def updateItem(hashKey: String, sortKey: String, t: T): Unit = {
    val maybeRequest = maybeSortKeyName map { sortKeyName =>
      new UpdateItemRequest().withTableName(tableName)
        .withKey(Map(
          keyName -> new AttributeValue(hashKey),
          sortKeyName -> new AttributeValue(sortKey)
        ))
        .withAttributeUpdates(toItemUpdate(t).mapValues(_.withAction(AttributeAction.PUT)))
    }

    maybeRequest foreach (updateThisItem(_))
  }

  def deleteItem[T: DynamoFormat](hashKey: String): Unit = {
    Scanamo.delete[String, T](dynamoDB)(tableName)(keyName -> hashKey)
  }

  def deleteItem[T: DynamoFormat](hashKey: String, sortKey: String): Unit = {
    maybeSortKeyName map { sortKeyName =>
      Scanamo.delete[String, T](dynamoDB)(tableName)(keyName -> hashKey, sortKeyName -> sortKey)
    }
  }

  def getItemAttributeValue(key: String, item: Map[String, AttributeValue]): AttributeValue = {
    item.getOrElse(key, {
      logger.warn(s"Provided key $key has no value.")
      new AttributeValue("")
    })
  }

  private def getListOfItems(request: QueryRequest) = {

    val promise = Promise[List[T]]()

    val responseHandler = new AsyncHandler[QueryRequest, QueryResult] {

      override def onError(e: Exception) = {
        logger.warn(s"Could not retrieve item: ${e.getMessage} ")
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

  private def updateThisItem(request: UpdateItemRequest) = {
    val promise = Promise[UpdateItemResult]()
    val responseHandler = new AsyncHandler[UpdateItemRequest, UpdateItemResult] {

      override def onError(e: Exception) = {
        logger.error(e.getMessage)
        promise.failure(e)
      }

      override def onSuccess(request: UpdateItemRequest, result: UpdateItemResult) = {
        promise.success(result)
      }

    }

    dynamoDB.updateItemAsync(request, responseHandler)
    promise.future
  }

}
