package com.gu.floodgate

import org.scanamo._
import org.scanamo.joda.JodaFormats.jodaStringFormat
import org.scanamo.auto._
import org.apache.pekko.actor.{ActorRef, ActorSystem}
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.dynamodbv2._
import com.gu.floodgate.contentsource.{ContentSourceApi, ContentSourceService, ContentSourceTable}
import com.gu.floodgate.jobhistory.{JobHistoryApi, JobHistoryService, JobHistoryTable}
import com.gu.floodgate.reindex.{BulkJobActor, ProgressTrackerController, ReindexService}
import com.gu.floodgate.runningjob.{RunningJobApi, RunningJobService, RunningJobTable}
import com.gu.floodgate.schedule.ReindexSchedule
import com.gu.googleauth.{AntiForgeryChecker, AuthAction, GoogleAuthConfig}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{BuiltInComponentsFromContext, NoHttpFiltersComponents}
import play.api.routing.Router
import controllers.{Application, AssetsComponents, Healthcheck, Login, routes}
import io.prometheus.client.hotspot.DefaultExports
import play.api.mvc.AnyContent
import play.api.mvc.legacy.Controller
import router.Routes

class AppComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with NoHttpFiltersComponents
    with AhcWSComponents
    with AssetsComponents {

  private val pekkoActorSystem = ActorSystem("floodgate")

  Controller.init(controllerComponents)

  val awsCredsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("capi"),
    InstanceProfileCredentialsProvider.getInstance()
  )

  val configEnvironment = configuration.get[String]("env")
  val isTest = configEnvironment == "TEST"
  val region = Region getRegion Regions.fromName(configuration.getOptional[String]("aws.region") getOrElse "eu-west-1")
  val clientConfiguration = new ClientConfiguration()

  //set up the default metrics
  DefaultExports.initialize()

  val dynamoDB: AmazonDynamoDBAsync = {
    val builder = AmazonDynamoDBAsyncClientBuilder.standard()
    builder.withRegion(region.toString)
    builder.withClientConfiguration(clientConfiguration)
    builder.withCredentials(awsCredsProvider)
    builder.build()
  }

  val scanamoSync = Scanamo(dynamoDB)
  val scanamoAsync = ScanamoAsync(dynamoDB)

  val contentSourceTable = {
    val tableName = configuration.getOptional[String]("aws.table.name.contentsource") getOrElse "floodgate-content-source-DEV"
    new ContentSourceTable(scanamoSync, scanamoAsync, tableName)
  }

  val jobHistoryTable = {
    val tableName = configuration.getOptional[String]("aws.table.name.jobhistory") getOrElse "floodgate-job-history-DEV"
    new JobHistoryTable(scanamoSync, scanamoAsync, tableName)
  }

  val runningJobTable = {
    val tableName = configuration.getOptional[String]("aws.table.name.runningjob") getOrElse "floodgate-running-job-DEV"
    new RunningJobTable(scanamoSync, scanamoAsync, tableName)
  }

  val runningJobService = new RunningJobService(runningJobTable)
  val contentSourceService = new ContentSourceService(contentSourceTable)
  val jobHistoryService = new JobHistoryService(jobHistoryTable)

  val progressTrackerControllerActor = pekkoActorSystem.actorOf(
    ProgressTrackerController.props(wsClient, runningJobService, jobHistoryService),
    "progress-tracker-controller"
  )

  val reindexService = new ReindexService(
    contentSourceService,
    runningJobService,
    progressTrackerControllerActor,
    wsClient
  )
  val runningJobController = new RunningJobApi(runningJobService)
  val jobHistoryController = new JobHistoryApi(jobHistoryService)

  if (!isTest) {
    val reindexScheduler = new ReindexSchedule(contentSourceService, reindexService)
    reindexScheduler.start()
  }

  val bulkActorsMap: Map[String, ActorRef] = {
    if (isTest) {
      Map()
    } else {
      Map(
        "draft-code" -> generateBulkActors("draft-code"),
        "live-code" -> generateBulkActors("live-code"),
        "draft-prod" -> generateBulkActors("draft-prod"),
        "live-prod" -> generateBulkActors("live-prod")
      )
    }
  }
  val contentSourceController =
    new ContentSourceApi(contentSourceService, reindexService, jobHistoryService, runningJobService, bulkActorsMap)

  val authConfig = {
    val clientId = configuration.get[String]("google.clientid")
    val clientSecret = configuration.get[String]("google.clientsecret")
    val redirectUrl = configuration.get[String]("google.oauthcallback")
    GoogleAuthConfig(
      clientId,
      clientSecret,
      redirectUrl,
      domains = List("guardian.co.uk"),
      maxAuthAge = Some(java.time.Duration.ofDays(90)),
      antiForgeryChecker = AntiForgeryChecker.borrowSettingsFromPlay(httpConfiguration)
    )
  }
  val authAction =
    new AuthAction[AnyContent](authConfig, routes.Login.loginAction, controllerComponents.parsers.default)(
      executionContext
    )

  val appController = new Application(authAction, authConfig, wsClient, configuration)
  val healthcheckController = new Healthcheck
  val loginController = new Login(authConfig, wsClient, configuration)

  val router: Router = new Routes(
    httpErrorHandler,
    appController,
    healthcheckController,
    loginController,
    contentSourceController,
    jobHistoryController,
    runningJobController,
    assets
  )

  def generateBulkActors(environment: String): ActorRef = {
    val contentSources = contentSourceTable.getAllSync()
    val idList = contentSources.filter(contentSource => contentSource.environment == environment).toList
    pekkoActorSystem.actorOf(
      BulkJobActor.props(idList, runningJobService, reindexService, jobHistoryService),
      s"${environment}-bulk-job"
    )
  }

}
