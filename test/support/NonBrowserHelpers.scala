package support

import play.api.http.{HeaderNames, HttpVerbs, Status}
import play.api.test.{DefaultAwaitTimeout, ResultExtractors}
import play.api.{ApplicationLoader, Play}
import org.specs2.execute.{AsResult, Result}

// Helpers also mixes in PlayRunners, which eagerly loads Selenium's browser drivers.
object NonBrowserHelpers
    extends HeaderNames
    with HttpVerbs
    with Status
    with DefaultAwaitTimeout
    with ResultExtractors

// Preserve the specs2 application lifecycle, without Helpers.running's browser drivers.
abstract class NonBrowserWithApplicationLoader(loader: ApplicationLoader)
    extends play.api.test.WithApplicationLoader(loader) {
  override def wrap[T: AsResult](body: => T): Result = NonBrowserHelpers.synchronized {
    try {
      Play.start(app)
      AsResult.effectively(body)
    } finally {
      Play.stop(app)
    }
  }
}
