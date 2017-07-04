package com.gu.floodgate.runningjob

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model._
import com.gu.floodgate.DynamoDBTable
import org.joda.time.DateTime

class RunningJobTable(protected val dynamoDB: AmazonDynamoDBAsync, protected val tableName: String)
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

  override protected def fromItem(item: Map[String, AttributeValue]): RunningJob = {
    RunningJob(
      getItemAttributeValue(fields.ContentSourceId, item).getS,
      getItemAttributeValue(fields.ContentSourceEnvironment, item).getS,
      BigDecimal(getItemAttributeValue(fields.DocumentsIndexed, item).getN).toInt,
      BigDecimal(getItemAttributeValue(fields.DocumentsExpected, item).getN).toInt,
      new DateTime(getItemAttributeValue(fields.StartTime, item).getS),
      for (v <- item.get(fields.RangeFrom); s <- Option(v.getS)) yield new DateTime(s),
      for (v <- item.get(fields.RangeTo); s <- Option(v.getS)) yield new DateTime(s)
    )
  }

  override protected def toItemUpdate(runningJob: RunningJob): Map[String, AttributeValueUpdate] =
    Map(
      fields.DocumentsIndexed -> new AttributeValueUpdate().withValue(new AttributeValue().withN(runningJob.documentsIndexed.toString)),
      fields.DocumentsExpected -> new AttributeValueUpdate().withValue(new AttributeValue().withN(runningJob.documentsExpected.toString)),
      fields.StartTime -> new AttributeValueUpdate().withValue(new AttributeValue(runningJob.startTime.toString))
    )

}
