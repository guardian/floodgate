
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{ InstanceProfileCredentialsProvider, SystemPropertiesCredentialsProvider, EnvironmentVariableCredentialsProvider, AWSCredentialsProviderChain }
import com.amazonaws.regions.{ Regions, Region }
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.gu.floodgate.contentsource.{ ContentSourceTable, ContentSourceService, ContentSourceApi }
import com.gu.floodgate.jobhistory.{ JobHistoryTable, JobHistoryService, JobHistoryApi }
import com.gu.floodgate.reindex.ReindexService
import com.gu.floodgate.runningjob.{ RunningJobTable, RunningJobService, RunningJobApi }
import com.gu.floodgate.{ Login, Application }
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ning.NingWSComponents
import play.api.{ BuiltInComponentsFromContext }
import play.api.routing.Router
import controllers.Assets
import router.Routes

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with NingWSComponents {

  val awsCredsProvider = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider(),
    new SystemPropertiesCredentialsProvider(),
    new ProfileCredentialsProvider("capi"),
    new ProfileCredentialsProvider(),
    new InstanceProfileCredentialsProvider()
  )

  val region = Region getRegion Regions.fromName(configuration.getString("aws.region") getOrElse "eu-west-1")
  val clientConfiguration = new ClientConfiguration()
  val dynamoDB = region.createClient(classOf[AmazonDynamoDBAsyncClient], awsCredsProvider, clientConfiguration)

  val contentSourceTable = {
    val tableName = configuration.getString("aws.table.name.contentsource") getOrElse "floodgate-content-source-DEV"
    new ContentSourceTable(dynamoDB, tableName)
  }

  val jobHistoryTable = {
    val tableName = configuration.getString("aws.table.name.jobhistory") getOrElse "floodgate-job-history-DEV"
    new JobHistoryTable(dynamoDB, tableName)
  }

  val runningJobTable = {
    val tableName = configuration.getString("aws.table.name.runningjob") getOrElse "floodgate-running-job-DEV"
    new RunningJobTable(dynamoDB, tableName)
  }

  val runningJobService = new RunningJobService(runningJobTable)
  val contentSourceService = new ContentSourceService(contentSourceTable)
  val jobHistoryService = new JobHistoryService(jobHistoryTable)
  val reindexService = new ReindexService(contentSourceService, runningJobService, wsApi)

  val contentSourceController = new ContentSourceApi(contentSourceService, reindexService, jobHistoryService)
  val runningJobController = new RunningJobApi(runningJobService)

  val jobHistoryController = new JobHistoryApi(jobHistoryService)

  val appController = new Application
  val loginController = new Login(wsApi)

  val assets = new Assets(httpErrorHandler)
  val router: Router = new Routes(httpErrorHandler, appController, loginController,
    contentSourceController, jobHistoryController, runningJobController, assets)

}