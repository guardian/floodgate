package com.gu.floodgate.jobhistory

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model.{ AttributeValueUpdate, AttributeValue }
import com.gu.floodgate.DynamoDBTable
import org.joda.time.DateTime

class JobHistoryTable(protected val dynamoDB: AmazonDynamoDBAsyncClient, protected val tableName: String)
    extends DynamoDBTable[JobHistory] {

  object fields {
    val Id = "id"
    val Status = "status"
    val StartTime = "startTime"
    val FinishTime = "finishTime"
  }

  override protected def fromItem(item: Map[String, AttributeValue]): JobHistory =
    JobHistory(
      getItemAttributeValue(fields.Id, item).getS,
      getItemAttributeValue(fields.Status, item).getS,
      new DateTime(getItemAttributeValue(fields.StartTime, item).getS),
      new DateTime(getItemAttributeValue(fields.FinishTime, item).getS)
    )

  override protected def toItem(jobHistory: JobHistory): Map[String, AttributeValue] =
    Map(fields.Id -> new AttributeValue(jobHistory.id),
      fields.Status -> new AttributeValue(jobHistory.status),
      fields.StartTime -> new AttributeValue(jobHistory.startTime.toString),
      fields.FinishTime -> new AttributeValue(jobHistory.finishTime.toString))

  override protected def toItemUpdate(jobHistory: JobHistory): Map[String, AttributeValueUpdate] =
    Map(fields.Id -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.id)),
      fields.Status -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.status.toString)),
      fields.StartTime -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.startTime.toString)),
      fields.FinishTime -> new AttributeValueUpdate().withValue(new AttributeValue(jobHistory.finishTime.toString)))

}
