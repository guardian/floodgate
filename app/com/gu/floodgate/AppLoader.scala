package com.gu.floodgate

import play.api.libs.logback.LogbackLoggerConfigurator
import play.api.{ Application, ApplicationLoader }
import play.api.ApplicationLoader.Context

class AppLoader extends ApplicationLoader {

  var c: AppComponents = null

  override def load(context: Context): Application = {
    new LogbackLoggerConfigurator().configure(context.environment)
    c = new AppComponents(context)
    c.application
  }
}