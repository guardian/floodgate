package com.gu.floodgate.contentsource

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, AttributeValueUpdate }
import com.gu.floodgate.DynamoDBTable
import scala.collection.JavaConverters._

class ContentSourceTable(protected val dynamoDB: AmazonDynamoDBAsyncClient, protected val tableName: String)
    extends DynamoDBTable[ContentSource] {

  object fields {
    val Id = "id"
    val Environment = "environment"
    val AuthType = "authType"
    val AppName = "appName"
    val Description = "description"
    val ReindexEndpoint = "reindexEndpoint"
    val ContentSourceSettings = "contentSourceSettings"
  }

  override protected val keyName: String = fields.Id
  override protected val maybeSortKeyName: Option[String] = Some(fields.Environment)

  override protected def fromItem(item: Map[String, AttributeValue]): ContentSource = {
    val contentSourceSettings = getItemAttributeValue(fields.ContentSourceSettings, item).getM.asScala.toMap

    ContentSource(
      getItemAttributeValue(fields.Id, item).getS,
      getItemAttributeValue(fields.AppName, item).getS,
      getItemAttributeValue(fields.Description, item).getS,
      getItemAttributeValue(fields.ReindexEndpoint, item).getS,
      getItemAttributeValue(fields.Environment, item).getS,
      getItemAttributeValue(fields.AuthType, item).getS,
      ContentSourceSettings(contentSourceSettings)
    )
  }

  override protected def toItem(contentSource: ContentSource): Map[String, AttributeValue] = {
    val contentSourceSettings = contentSource.contentSourceSettings.toMap.mapValues(new AttributeValue().withBOOL(_)).asJava

    Map(fields.Id -> new AttributeValue(contentSource.id),
      fields.AppName -> new AttributeValue(contentSource.appName),
      fields.Description -> new AttributeValue(contentSource.description),
      fields.ReindexEndpoint -> new AttributeValue(contentSource.reindexEndpoint),
      fields.Environment -> new AttributeValue(contentSource.environment),
      fields.AuthType -> new AttributeValue(contentSource.authType),
      fields.ContentSourceSettings -> new AttributeValue().withM(contentSourceSettings))
  }

  override protected def toItemUpdate(contentSource: ContentSource): Map[String, AttributeValueUpdate] = {
    val contentSourceSettings = contentSource.contentSourceSettings.toMap.mapValues(new AttributeValue().withBOOL(_)).asJava

    Map(fields.AppName -> new AttributeValueUpdate().withValue(new AttributeValue(contentSource.appName)),
      fields.Description -> new AttributeValueUpdate().withValue(new AttributeValue(contentSource.description)),
      fields.ReindexEndpoint -> new AttributeValueUpdate().withValue(new AttributeValue(contentSource.reindexEndpoint)),
      fields.AuthType -> new AttributeValueUpdate().withValue(new AttributeValue(contentSource.authType)),
      fields.ContentSourceSettings -> new AttributeValueUpdate().withValue(new AttributeValue().withM(contentSourceSettings)))
  }
}
