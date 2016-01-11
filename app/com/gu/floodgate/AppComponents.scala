import com.gu.floodgate.Application
import controllers.Assets
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ning.NingWSComponents
import play.api.{ BuiltInComponentsFromContext }
import play.api.routing.Router
import router.Routes

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with NingWSComponents {

  val appController = new Application
  val assets = new Assets(httpErrorHandler)
  val router: Router = new Routes(httpErrorHandler, appController, assets)

}