package com.gu.floodgate.contentsource

import java.util.UUID

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.gu.floodgate.reindex.DateParameters

/*
 * Used for creation as we generate the ID server side and do not expect the client to provide it.
 */

case class ContentSourceWithoutId(
    appName: String,
    description: String,
    reindexEndpoint: String,
    environment: String,
    authType: String,
    contentSourceSettings: ContentSourceSettings,
    headers: Option[Map[String, String]]
)

case class ContentSourcesResponse(contentSources: Seq[ContentSource])

case class SingleContentSourceResponse(contentSource: ContentSource)

case class ContentSourceSettings(supportsToFromParams: Boolean = true, supportsCancelReindex: Boolean = true) {

  def toMap = {
    Map(
      ContentSourceSettings.fields.SupportsToFromParams -> this.supportsToFromParams,
      ContentSourceSettings.fields.SupportsCancelReindex -> this.supportsCancelReindex
    )
  }
}

object ContentSourceSettings {

  object fields {
    val SupportsToFromParams = "supportsToFromParams"
    val SupportsCancelReindex = "supportsCancelReindex"
  }

  def apply(settings: Map[String, AttributeValue]): ContentSourceSettings = {
    val supportsToFromParams =
      settings.getOrElse(ContentSourceSettings.fields.SupportsToFromParams, new AttributeValue("true")).getBOOL
    val supportsCancelReindex =
      settings.getOrElse(ContentSourceSettings.fields.SupportsCancelReindex, new AttributeValue("true")).getBOOL

    ContentSourceSettings(supportsToFromParams, supportsCancelReindex)
  }
}

case class ContentSource(
    id: String,
    appName: String,
    description: String,
    reindexEndpoint: String,
    environment: String,
    authType: String,
    contentSourceSettings: ContentSourceSettings,
    headers: Option[Map[String, String]]
) {

  def uniqueId: String = s"$id-$environment"

  def reindexEndpointWithDateParams(dateParameters: DateParameters): String = {

    def param(key: String, value: Option[String]): Option[String] = value.map(v => s"$key=$v")

    val paramFrom = param("from", dateParameters.from map (_.toString))
    val paramTo = param("to", dateParameters.to map (_.toString))
    val queryString: String = Seq(paramFrom, paramTo).flatten.mkString("&")
    val urlSeparator = if (reindexEndpoint contains "?") "&" else "?"

    s"$reindexEndpoint$urlSeparator$queryString"
  }

}

/*
 * Used for updates as we do not allow the user to update their id or environment, which act as hashkey and sort key in
 * the DB.
 */
case class ContentWithoutIdAndEnvironment(
    appName: String,
    description: String,
    reindexEndpoint: String,
    authType: String,
    contentSourceSettings: ContentSourceSettings,
    headers: Option[Map[String, String]]
)

object ContentSource {

  def apply(contentSourceWithoutId: ContentSourceWithoutId): ContentSource = {
    val id = UUID.randomUUID().toString
    ContentSource(id, contentSourceWithoutId)
  }

  def apply(id: String, contentSourceWithoutId: ContentSourceWithoutId): ContentSource = {
    ContentSource(
      id,
      contentSourceWithoutId.appName,
      contentSourceWithoutId.description,
      contentSourceWithoutId.reindexEndpoint,
      contentSourceWithoutId.environment,
      contentSourceWithoutId.authType,
      contentSourceWithoutId.contentSourceSettings,
      contentSourceWithoutId.headers
    )
  }

  def apply(id: String, environment: String, contentSource: ContentWithoutIdAndEnvironment): ContentSource = {
    ContentSource(
      id,
      contentSource.appName,
      contentSource.description,
      contentSource.reindexEndpoint,
      environment,
      contentSource.authType,
      contentSource.contentSourceSettings,
      contentSource.headers
    )
  }
}
