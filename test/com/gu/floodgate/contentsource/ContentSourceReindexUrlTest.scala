package com.gu.floodgate.contentsource

import com.gu.floodgate.reindex.DateParameters
import org.joda.time.DateTime
import org.scalatest.{ Matchers, FlatSpec }

class ContentSourceReindexUrlTest extends FlatSpec with Matchers {

  val contentSourceWithApiKeyAuth =
    ContentSource(
      id = "id",
      appName = "my reindexer",
      description = "description of my reindexer",
      reindexEndpoint = "http://myurl.com/reindex?api-key=my-key",
      environment = "code-live",
      authType = "api-key")

  val contentSourceWithVpcPeerAuth = ContentSource(
    id = "id",
    appName = "my reindexer",
    description = "description of my reindexer",
    reindexEndpoint = "http://myurl.com/reindex",
    environment = "code-live",
    authType = "vpc-peered")

  it should "return the correct url to initiate a reindex when using api-key auth" in {
    val from = new DateTime("2016-01-01T00:00:00Z")
    val to = new DateTime("2016-01-31T00:00:00Z")
    val dateParameters = DateParameters(Some(from), Some(to))

    val reindexEndpoint = contentSourceWithApiKeyAuth.reindexEndpoint
    val reindexUrl = contentSourceWithApiKeyAuth.reindexEndpointWithDateParams(dateParameters)

    reindexUrl should be(s"${reindexEndpoint}&from=${from.toString}&to=$to")
  }

  it should "return the correct url to initiate a reindex when using vpc-peered auth" in {
    val from = new DateTime("2016-01-01T00:00:00Z")
    val to = new DateTime("2016-01-31T00:00:00Z")
    val dateParameters = DateParameters(Some(from), Some(to))

    val reindexEndpoint = contentSourceWithVpcPeerAuth.reindexEndpoint
    val reindexUrl = contentSourceWithVpcPeerAuth.reindexEndpointWithDateParams(dateParameters)

    reindexUrl should be(s"${reindexEndpoint}?from=${from.toString}&to=$to")
  }

}
