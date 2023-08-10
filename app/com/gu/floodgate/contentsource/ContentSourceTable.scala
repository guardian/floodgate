package com.gu.floodgate.contentsource

import com.gu.floodgate.DynamoDBTable
import org.scanamo.{DynamoFormat, Scanamo, ScanamoAsync}

class ContentSourceTable(
    protected val scanamoSync: Scanamo,
    protected val scanamoAsync: ScanamoAsync,
    protected val tableName: String
)(implicit override val D: DynamoFormat[ContentSource])
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

  final override protected val keyName: String = fields.Id
  final override protected val maybeSortKeyName: Option[String] = Some(fields.Environment)
}
