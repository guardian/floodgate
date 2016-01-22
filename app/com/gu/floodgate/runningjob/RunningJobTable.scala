package com.gu.floodgate.runningjob

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model.{ AttributeValueUpdate, AttributeValue }
import com.gu.floodgate.DynamoDBTable
import org.joda.time.DateTime

class RunningJobTable(protected val dynamoDB: AmazonDynamoDBAsyncClient, protected val tableName: String)
    extends DynamoDBTable[RunningJob] {

  object fields {
    val Id = "id"
    val Progress = "progress"
    val StartTime = "startTime"
  }

  override protected def fromItem(item: Map[String, AttributeValue]): RunningJob =
    RunningJob(
      getItemAttributeValue(fields.Id, item).getS,
      getItemAttributeValue(fields.Progress, item).getN.toDouble,
      new DateTime(getItemAttributeValue(fields.StartTime, item).getS)
    )

  override protected def toItem(runningJob: RunningJob): Map[String, AttributeValue] =
    Map(fields.Id -> new AttributeValue(runningJob.id),
      fields.Progress -> new AttributeValue(runningJob.progress.toString),
      fields.StartTime -> new AttributeValue(runningJob.startTime.toString))

  override protected def toItemUpdate(runningJob: RunningJob): Map[String, AttributeValueUpdate] =
    Map(fields.Progress -> new AttributeValueUpdate().withValue(new AttributeValue(runningJob.progress.toString)),
      fields.StartTime -> new AttributeValueUpdate().withValue(new AttributeValue(runningJob.startTime.toString)))
}
