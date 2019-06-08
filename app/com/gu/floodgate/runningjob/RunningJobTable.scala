package com.gu.floodgate.runningjob

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model._
import com.gu.floodgate.DynamoDBTable
import org.joda.time.DateTime
import org.scanamo.{DynamoFormat, Scanamo, ScanamoAsync}

class RunningJobTable(
    protected val scanamoSync: Scanamo,
    protected val scanamoAsync: ScanamoAsync,
    protected val tableName: String
)(implicit override val D: DynamoFormat[RunningJob])
    extends DynamoDBTable[RunningJob] {

  object fields {
    val ContentSourceId = "contentSourceId"
    val ContentSourceEnvironment = "contentSourceEnvironment"
    val DocumentsIndexed = "documentsIndexed"
    val DocumentsExpected = "documentsExpected"
    val StartTime = "startTime"
    val RangeFrom = "rangeFrom"
    val RangeTo = "rangeTo"
  }

  override protected val keyName = fields.ContentSourceId
  override protected val maybeSortKeyName = Some(fields.ContentSourceEnvironment)

}
