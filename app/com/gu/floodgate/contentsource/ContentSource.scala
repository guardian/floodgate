package com.gu.floodgate.contentsource

// TODO Will need to store auth credentials (in a safe manner) once we know more.
case class ContentSource(id: String, appName: String, description: String, reindexEndpoint: String)
