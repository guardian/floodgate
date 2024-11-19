package com.gu.floodgate.contentsource

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ContentSourceSpec extends AnyFlatSpec with Matchers  {
  val contentSourceWithoutId = ContentSourceWithoutId(
    appName = "my reindexer",
    description = "description of my reindexer",
    reindexEndpoint = "http://myurl.com/reindex?api-key=my-key",
    environment = "code-live",
    authType = "api-key",
    contentSourceSettings = ContentSourceSettings(true, true)
  )

  val contentSourceWithoutIdAndEnvironment = ContentWithoutIdAndEnvironment(
    appName = "my reindexer",
    description = "description of my reindexer",
    reindexEndpoint = "http://myurl.com/reindex?api-key=my-key",
    authType = "api-key",
    contentSourceSettings = ContentSourceSettings(true, true)
  )

  it should "add a UUID when a content source is created from a ContentSourceWithoutId" in {
    val contentSource = ContentSource(contentSourceWithoutId)
    contentSource.id.length shouldBe 36
  }

  it should "preserve the current ID when a content source is created from a ContentSourceWithoutId and an id is supplied" in {
    val contentSource = ContentSource("explicit-id", contentSourceWithoutId)
    contentSource.id shouldBe "explicit-id"
  }

  it should "preserve the ID when a content source is created from a ContentWithoutIdAndEnvironment but an id is supplied" in {
    val contentSource = ContentSource("explicit-id", "code-live", contentSourceWithoutIdAndEnvironment)
    contentSource.id shouldBe "explicit-id"
  }
}
