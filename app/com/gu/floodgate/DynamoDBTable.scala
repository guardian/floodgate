package com.gu.floodgate

import cats.data.ValidatedNel
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.handlers.AsyncHandler
import org.scanamo.error.DynamoReadError
import org.scanamo.query._
import org.scanamo._
import org.scanamo.syntax._
import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._
import scala.concurrent.{ ExecutionContext, Future, Promise }

trait DynamoDBTable[T] extends StrictLogging {

  implicit def D: DynamoFormat[T]

  protected val scanamoSync: Scanamo
  protected val scanamoAsync: ScanamoAsync
  protected val tableName: String
  protected val keyName: String
  protected val table: Table[T] = Table[T](tableName)
  protected val maybeSortKeyName: Option[String] = None

  final private def reportErrors(results: List[Either[DynamoReadError, T]]): List[T] = results.flatMap(_.fold(
    error => {
      logger.error(error.toString)
      None
    },
    result => Some(result)
))

  def getAll()(implicit ec: ExecutionContext): Future[Seq[T]] = scanamoAsync.exec(table.scan).map(reportErrors)

  def getAllSync(): List[T] = reportErrors(scanamoSync.exec(table.scan))

  def getItem(hashKey: String, sortKey: String): Option[Either[DynamoReadError, T]] = {
    maybeSortKeyName flatMap { sortKeyName =>
      scanamoSync.exec(table.get((Symbol(keyName) -> hashKey) and (Symbol(sortKeyName) -> sortKey)))
    } orElse None
  }

  def getItems(id: String)(implicit ec: ExecutionContext): Future[List[T]] = scanamoAsync.exec(table.descending.query(Symbol(keyName) -> id)).map(reportErrors)

  def getItemsWithFilter(id: String, filterKeyName: String, filterValue: String)(implicit ec: ExecutionContext): Future[List[T]] = 
    scanamoAsync.exec(table.descending.filter(Condition(Symbol(filterKeyName) -> filterValue)).query(Symbol(keyName) -> id)).map(reportErrors)

  def getLatestItem(id: String, filterKeyName: String, filterValue: String)(implicit ec: ExecutionContext): Future[Option[T]] =
    scanamoAsync.exec(table.descending.limit(1).filter(Condition(Symbol(filterKeyName) -> filterValue)).query(Symbol(keyName) -> id)).map(reportErrors).map(_.headOption)

  def saveItem(t: T): Option[Either[DynamoReadError,T]] = scanamoSync.exec(table.put(t))

  def deleteItem(hashKey: String): Unit = scanamoSync.exec(table.delete(Symbol(keyName) -> hashKey))

  def deleteItem(hashKey: String, sortKey: String): Unit = {
    maybeSortKeyName map { sortKeyName =>
      scanamoSync.exec(table.delete((Symbol(keyName) -> hashKey) and (Symbol(sortKeyName) -> sortKey)))
    }
  }
}
