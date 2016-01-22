package com.gu.floodgate.contentsource

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model.{ AttributeValueUpdate, AttributeValue }
import com.gu.floodgate.DynamoDBTable

class ContentSourceTable(protected val dynamoDB: AmazonDynamoDBAsyncClient, protected val tableName: String)
    extends DynamoDBTable[ContentSource] {

  object fields {
    val Id = "id"
    val AppName = "appName"
    val Description = "description"
    val ReindexEndpoint = "reindexEndpoint"
  }

  override protected def fromItem(item: Map[String, AttributeValue]): ContentSource =
    ContentSource(
      getItemAttributeValue(fields.Id, item).getS,
      getItemAttributeValue(fields.AppName, item).getS,
      getItemAttributeValue(fields.Description, item).getS,
      getItemAttributeValue(fields.ReindexEndpoint, item).getS)

  override protected def toItem(contentSource: ContentSource): Map[String, AttributeValue] =
    Map(fields.Id -> new AttributeValue(contentSource.id),
      fields.AppName -> new AttributeValue(contentSource.appName),
      fields.Description -> new AttributeValue(contentSource.description),
      fields.ReindexEndpoint -> new AttributeValue(contentSource.reindexEndpoint))

  override protected def toItemUpdate(contentSource: ContentSource): Map[String, AttributeValueUpdate] =
    Map(fields.AppName -> new AttributeValueUpdate().withValue(new AttributeValue(contentSource.appName)),
      fields.Description -> new AttributeValueUpdate().withValue(new AttributeValue(contentSource.description)),
      fields.ReindexEndpoint -> new AttributeValueUpdate().withValue(new AttributeValue(contentSource.reindexEndpoint)))

}
