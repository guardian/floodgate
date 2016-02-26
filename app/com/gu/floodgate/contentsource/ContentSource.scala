package com.gu.floodgate.contentsource

import java.util.UUID
import com.gu.floodgate.reindex.DateParameters
import play.json.extra.JsonFormat

/*
 * Used for creation as we generate the ID server side and do not expect the client to provide it.
 */
@JsonFormat
case class ContentSourceWithoutId(
  appName: String,
  description: String,
  reindexEndpoint: String,
  environment: String,
  authType: String)

@JsonFormat
case class ContentSourcesResponse(contentSources: Seq[ContentSource])

@JsonFormat
case class SingleContentSourceResponse(contentSource: ContentSource)

@JsonFormat
case class ContentSource(
    id: String,
    appName: String,
    description: String,
    reindexEndpoint: String,
    environment: String,
    authType: String) {

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
 * Used for updates as we do not allow the user to update their id or environment which act as hashkey and sort key in
 * the DB.
 */
@JsonFormat
case class ContentWithoutIdAndEnvironment(
  appName: String,
  description: String,
  reindexEndpoint: String,
  authType: String)

object ContentSource {

  def apply(contentSourceWithoutId: ContentSourceWithoutId): ContentSource = {
    val id = UUID.randomUUID().toString
    ContentSource(id, contentSourceWithoutId.appName, contentSourceWithoutId.description,
      contentSourceWithoutId.reindexEndpoint, contentSourceWithoutId.environment, contentSourceWithoutId.authType)
  }

  def apply(id: String, environment: String, contentSource: ContentWithoutIdAndEnvironment): ContentSource = {
    val id = UUID.randomUUID().toString
    ContentSource(id, contentSource.appName, contentSource.description, contentSource.reindexEndpoint, environment, contentSource.authType)
  }
}
