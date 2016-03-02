# Implementing a content source

This document provides instructions for integrating a new content source with CAPI and Floodgate. The steps are:

1. Make the content indexable by sending all content update events to a Kinesis stream

2. Make the content *re-indexable* by implementing a reindexing endpoint

3. Allow Floodgate to track reindex progress by requesting data from your reindex endpoint

## Make content indexable in CAPI

Every time an piece of content in your database is updated, your app should send an event to a Kinesis stream. Porter will consume these events and write their contents to Elasticsearch.

Events should be Thrift encoded and compressed, and should contain a complete representation of the item that has been updated. In other words, it should NOT contain only a delta describing what has changed.

The Kinesis stream should be in your AWS account, and you should provide a role with permissions to consume from the stream. Porter will assume this role.

### Thrift schema

You should publish a jar containing the Thrift schema files, properly versioned, to Bintray or Maven Central. Porter will need these files in order to deserialize your events.

### Compression

Events should consist of a payload, encoded using the Thrift Compact protocol, prefixed by a one byte header. This header describes how the payload has been compressed:

* `0x00` = no compression
* `0x01` = gzipped

## Make content reindexable

In order to make it possible to reindex some or all of the items in your datastore, you need to provide 2 things:

* A separate Kinesis stream reserved for reindexing. This is to avoid starving the indexing of normal updates while a reindex operation is running. Don't forget that Porter's role will need the permissions to consume from both Kinesis streams.

* An endpoint for triggering a reindex and retrieving information about a reindex. Floodgate will programmatically access this endpoint.

### Reindex endpoint

When the endpoint receives a `POST` request with an empty body, your app should find all (or some, depending on query parameters described below) the items in its datastore and send update events for those items to the reindexing stream.

#### (optional) Query parameters

If your datastore contains a large number of items and a full reindex thus takes a long time to complete, your reindex endpoint should support `from` and `to` query parameters. These parameters should accept ISO 8601 timestamps (e.g. `2016-01-01T00:00+01:00`). If the `from` and/or `to` parameters are present, your app should only reindex items that match the filter. Depending on your app, you might filter on creation date or last-modified date.

#### Authentication

If your endpoint is open to the world, it should require an API key for authentication. Floodgate can pass this as a URL query parameter, e.g. `?api-key=foo`.

If your endpoint is only accessible via VPC peering then we deem API key authentication to be overkill, but you can require an API key if you like.

#### Simultaneous reindex operations

Your app should make a best-effort attempt to prevent people from running multiple reindexes at the same time. If the endpoint receives a `POST` when a reindex is already running, it should return a `403 Forbidden`.

This restriction is also implemented on the Floodgate side: Floodgate will never try to trigger a reindex of your data until the currently running reindex has completed.

#### Example

A call to your endpoint to trigger a reindex might look something like this:

```
POST /reindex?api-key=foo&from=2016-01-01T00:00Z&to=2016-02-29T23:59:59Z
```

## Provide information about reindex progress

A `GET` request to your endpoint should return information about *either* the currently running reindex, if there is one, *or* the last completed reindex.

It should return a JSON object containing 3 fields:

* `status` (string) - one of `in progress`, `failed`, `completed`, `cancelled`
* `documentsIndexed` (number) - how many items have been sent to the reindexing stream so far
* `documentsExpected` (number) - how many items you expect to send in total

e.g. when a reindex is in progress

```
GET /reindex
{ "status": "in progress", "documentsIndexed": 123, "documentsExpected": 400 }
```

e.g. when a reindex has completed

```
GET /reindex
{ "status": "completed", "documentsIndexed": 400, "documentsExpected": 400 }
```

If there is no running reindex and no reindex has ever run before, the endpoint should return a 404 response.

## (optional) Support cancellation

If a reindex takes a long time to complete, your app should support cancellation of a reindex.

To cancel a reindex, Floodgate will send a `DELETE` request to your endpoint.

If there is no running reindex, the endpoint should return a 404 response.

## Recap of reindex endpoint requirements

Your reindex endpoint should accept:

* `POST` - trigger a reindex. Can optionally support `from` and `to` parameters and an API key parameter
* `GET` - return progress information about the currently running reindex or the last completed reindex
* (optional) `DELETE` - cancel a running reindex

## Reference implementation

Floodgate contains a dummy implementation of a reindex endpoint which you may find useful as a reference. See [this test](https://github.com/guardian/floodgate/blob/master/test/com/gu/floodgate/FloodgateIntegrationSpec.scala) for examples of how Floodgate will interact with your endpoint.

## Registering your content source with Floodgate

You can add your content source to Floodgate using the UI. Just go to [https://floodgate.capi.gutools.co.uk/](https://floodgate.capi.gutools.co.uk) and click 'Register'.

If you have multiple environments (e.g. PROD live, CODE preview, etc.), you can register separate endpoints for each one.
