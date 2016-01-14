
import com.gu.floodgate.contentsource.{ ContentSourceService, ContentSourceApi }
import com.gu.floodgate.jobhistory.{ JobHistoryService, JobHistoryApi }
import com.gu.floodgate.runningjob.{ RunningJobService, RunningJobApi }
import com.gu.floodgate.{ Login, Application }
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ning.NingWSComponents
import play.api.{ BuiltInComponentsFromContext }
import play.api.routing.Router
import controllers.Assets
import router.Routes

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with NingWSComponents {

  val contentSourceService = new ContentSourceService
  val contentSourceController = new ContentSourceApi(contentSourceService)

  val jobHistoryService = new JobHistoryService
  val jobHistoryController = new JobHistoryApi(jobHistoryService)

  val runningJobService = new RunningJobService
  val runningJobController = new RunningJobApi(runningJobService)

  val appController = new Application
  val loginController = new Login(wsApi)
  val assets = new Assets(httpErrorHandler)
  val router: Router = new Routes(httpErrorHandler, appController, loginController,
    contentSourceController, jobHistoryController, runningJobController, assets)

}