
import play.api.{ Logger, Application, ApplicationLoader }
import play.api.ApplicationLoader.Context

class AppLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    Logger.configure(context.environment)
    new AppComponents(context).application
  }
}