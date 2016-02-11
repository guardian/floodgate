package com.gu.floodgate.jobhistory

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model.{ AttributeValueUpdate, AttributeValue }
import com.gu.floodgate.DynamoDBTable
import org.joda.time.DateTime

class JobHistoryTable(protected val dynamoDB: AmazonDynamoDBAsyncClient, protected val tableName: String)
    extends DynamoDBTable[JobHistory] {

  object fields {
    val ContentSourceId = "contentSourceId"
    val Status = "status"
    val StartTime = "startTime"
    val FinishTime = "finishTime"
  }

  override protected val keyName: String = fields.ContentSourceId

  override protected def fromItem(item: Map[String, AttributeValue]): JobHistory =
    JobHistory(
      getItemAttributeValue(fields.ContentSourceId, item).getS,
      new DateTime(getItemAttributeValue(fields.StartTime, item).getS),
      new DateTime(getItemAttributeValue(fields.FinishTime, item).getS),
      getItemAttributeValue(fields.Status, item).getS
    )

  override protected def toItem(jobHistory: JobHistory): Map[String, AttributeValue] =
    Map(fields.ContentSourceId -> new AttributeValue(jobHistory.contentSourceId),
      fields.StartTime -> new AttributeValue(jobHistory.startTime.toString),
      fields.FinishTime -> new AttributeValue(jobHistory.finishTime.toString),
      fields.Status -> new AttributeValue(jobHistory.status))

  override protected def toItemUpdate(jobHistory: JobHistory): Map[String, AttributeValueUpdate] =
    Map(
      fields.StartTime -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.startTime.toString)),
      fields.FinishTime -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.finishTime.toString)),
      fields.Status -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.status.toString)))

}
