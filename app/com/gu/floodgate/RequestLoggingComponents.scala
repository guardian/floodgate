package com.gu.floodgate

import play.api.BuiltInComponents
import play.api.mvc.EssentialFilter
import requestlogging._

trait RequestLoggingComponents extends BuiltInComponents {
  override lazy val httpFilters: Seq[EssentialFilter] =
    Seq(new RequestLoggingFilter(new RequestLoggingConfig(buildCommitId = Some(app.BuildInfo.gitCommitId))))
}

