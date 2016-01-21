package com.gu.floodgate.contentsource

import java.util.UUID
import play.json.extra.JsonFormat

@JsonFormat
case class ContentSourceWithoutId(appName: String, description: String, reindexEndpoint: String)

@JsonFormat
case class ContentSourcesResponse(contentSources: Seq[ContentSource])

@JsonFormat
case class SingleContentSourceResponse(contentSource: ContentSource)

@JsonFormat
case class ContentSource(id: String = UUID.randomUUID().toString,
  appName: String,
  description: String,
  reindexEndpoint: String)

object ContentSource {
  def apply(contentSourceWithoutId: ContentSourceWithoutId): ContentSource = {
    val id = UUID.randomUUID().toString

    ContentSource(id, contentSourceWithoutId.appName, contentSourceWithoutId.description,
      contentSourceWithoutId.reindexEndpoint)
  }
}
