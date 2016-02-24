package com.gu.floodgate.contentsource

import java.util.UUID
import play.json.extra.JsonFormat

/*
 * Used for creation as we generate the ID server side and do not expect the client to provide it.
 */
@JsonFormat
case class ContentSourceWithoutId(appName: String, description: String, reindexEndpoint: String, environment: String)

@JsonFormat
case class ContentSourcesResponse(contentSources: Seq[ContentSource])

@JsonFormat
case class SingleContentSourceResponse(contentSource: ContentSource)

@JsonFormat
case class ContentSource(id: String,
    appName: String,
    description: String,
    reindexEndpoint: String,
    environment: String) {

  def uniqueId: String = s"$id-$environment"
}

/*
 * Used for updates as we do not allow the user to update their id or environment which act as hashkey and sort key in
 * the DB.
 */
@JsonFormat
case class ContentWithoutIdAndEnvironment(appName: String, description: String, reindexEndpoint: String)

object ContentSource {

  def apply(contentSourceWithoutId: ContentSourceWithoutId): ContentSource = {
    val id = UUID.randomUUID().toString
    ContentSource(id, contentSourceWithoutId.appName, contentSourceWithoutId.description,
      contentSourceWithoutId.reindexEndpoint, contentSourceWithoutId.environment)
  }

  def apply(id: String, environment: String, contentSourceForUpdates: ContentWithoutIdAndEnvironment): ContentSource = {
    val id = UUID.randomUUID().toString
    ContentSource(id, contentSourceForUpdates.appName, contentSourceForUpdates.description,
      contentSourceForUpdates.reindexEndpoint, environment)
  }
}
