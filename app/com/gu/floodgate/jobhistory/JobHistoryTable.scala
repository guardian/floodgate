package com.gu.floodgate.jobhistory

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, AttributeValueUpdate }
import com.gu.floodgate.DynamoDBTable
import com.gu.floodgate.reindex._
import org.joda.time.DateTime

class JobHistoryTable(protected val dynamoDB: AmazonDynamoDBAsync, protected val tableName: String)
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

  override protected def fromItem(item: Map[String, AttributeValue]): JobHistory = {
    JobHistory(
      getItemAttributeValue(fields.ContentSourceId, item).getS,
      new DateTime(getItemAttributeValue(fields.StartTime, item).getS),
      new DateTime(getItemAttributeValue(fields.FinishTime, item).getS),
      ReindexStatus.fromString(getItemAttributeValue(fields.Status, item).getS).getOrElse(Unknown),
      getItemAttributeValue(fields.Environment, item).getS,
      for (v <- item.get(fields.RangeFrom); s <- Option(v.getS)) yield new DateTime(s),
      for (v <- item.get(fields.RangeTo); s <- Option(v.getS)) yield new DateTime(s)
    )
  }

  override protected def toItemUpdate(jobHistory: JobHistory): Map[String, AttributeValueUpdate] =
    Map(
      fields.StartTime -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.startTime.toString)),
      fields.FinishTime -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.finishTime.toString)),
      fields.Status -> new AttributeValueUpdate().withValue(new AttributeValue(ReindexStatus.asString(jobHistory.status))),
      fields.Environment -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.environment))
    )

}
