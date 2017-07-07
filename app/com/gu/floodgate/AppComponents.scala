package com.gu.floodgate

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{ AWSCredentialsProviderChain, InstanceProfileCredentialsProvider }
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.dynamodbv2._
import com.gu.floodgate.contentsource.{ ContentSourceApi, ContentSourceService, ContentSourceTable }
import com.gu.floodgate.jobhistory.{ JobHistoryApi, JobHistoryService, JobHistoryTable }
import com.gu.floodgate.reindex.{ ProgressTrackerController, ReindexService }
import com.gu.floodgate.runningjob.{ RunningJobApi, RunningJobService, RunningJobTable }
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{ BuiltInComponentsFromContext, NoHttpFiltersComponents }
import play.api.routing.Router
import controllers.{ Application, AssetsComponents, Healthcheck, Login }
import play.api.mvc.legacy.Controller
import router.Routes

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with NoHttpFiltersComponents with AhcWSComponents with AssetsComponents {

  Controller.init(controllerComponents)

  val awsCredsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("capi"),
    InstanceProfileCredentialsProvider.getInstance()
  )

  val region = Region getRegion Regions.fromName(configuration.getOptional[String]("aws.region") getOrElse "eu-west-1")
  val clientConfiguration = new ClientConfiguration()

  val dynamoDB: AmazonDynamoDBAsync = {
    val builder = AmazonDynamoDBAsyncClientBuilder.standard()
    builder.withRegion(region.toString)
    builder.withClientConfiguration(clientConfiguration)
    builder.withCredentials(awsCredsProvider)
    builder.build()
  }

  val contentSourceTable = {
    val tableName = configuration.getOptional[String]("aws.table.name.contentsource") getOrElse "floodgate-content-source-DEV"
    new ContentSourceTable(dynamoDB, tableName)
  }

  val jobHistoryTable = {
    val tableName = configuration.getOptional[String]("aws.table.name.jobhistory") getOrElse "floodgate-job-history-DEV"
    new JobHistoryTable(dynamoDB, tableName)
  }

  val runningJobTable = {
    val tableName = configuration.getOptional[String]("aws.table.name.runningjob") getOrElse "floodgate-running-job-DEV"
    new RunningJobTable(dynamoDB, tableName)
  }

  val runningJobService = new RunningJobService(runningJobTable)
  val contentSourceService = new ContentSourceService(contentSourceTable)
  val jobHistoryService = new JobHistoryService(jobHistoryTable)

  val progressTrackerControllerActor = actorSystem.actorOf(
    ProgressTrackerController.props(wsClient, runningJobService, jobHistoryService), "progress-tracker-controller"
  )

  val reindexService = new ReindexService(contentSourceService, runningJobService, jobHistoryService, progressTrackerControllerActor, wsClient)

  val contentSourceController = new ContentSourceApi(contentSourceService, reindexService, jobHistoryService, runningJobService)
  val runningJobController = new RunningJobApi(runningJobService)
  val jobHistoryController = new JobHistoryApi(jobHistoryService)

  val appController = new Application(wsClient, configuration)

  val healthcheckController = new Healthcheck

  val loginController = new Login(wsClient, configuration)

  val router: Router = new Routes(httpErrorHandler, appController, healthcheckController, loginController,
    contentSourceController, jobHistoryController, runningJobController, assets)

}