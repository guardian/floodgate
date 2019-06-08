package com.gu.floodgate.jobhistory

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, AttributeValueUpdate}
import com.gu.floodgate.DynamoDBTable
import com.gu.floodgate.reindex._
import org.joda.time.DateTime
import org.scanamo.{DynamoFormat, Scanamo, ScanamoAsync}

class JobHistoryTable(
    protected val scanamoSync: Scanamo,
    protected val scanamoAsync: ScanamoAsync,
    protected val tableName: String
)(implicit override val D: DynamoFormat[JobHistory])
    extends DynamoDBTable[JobHistory] {

  object fields {
    val ContentSourceId = "contentSourceId"
    val Status = "status"
    val StartTime = "startTime"
    val FinishTime = "finishTime"
    val Environment = "environment"
    val RangeFrom = "rangeFrom"
    val RangeTo = "rangeTo"
  }

  override protected val keyName: String = fields.ContentSourceId
  override protected val maybeSortKeyName: Option[String] = Some(fields.StartTime)

}
