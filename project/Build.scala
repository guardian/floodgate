import com.gu.riffraff.artifact.RiffRaffArtifact
import com.typesafe.sbt.packager.universal.UniversalPlugin
import sbt._
import Keys._
import play.sbt._
import play.sbt.Play.autoImport._
import play.sbt.routes.RoutesKeys._
import com.typesafe.sbt.SbtScalariform._
import RiffRaffArtifact.autoImport._
import UniversalPlugin.autoImport._


object FloodgateBuild extends Build {

  val basicSettings = Seq(
    organization := "com.gu",
    description := "Floodgate - The content API reindexing control panel",
    scalaVersion := "2.11.8",
    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-target:jvm-1.8")
  )

  lazy val project = Project(id = "content-api-floodgate", base = file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(RiffRaffArtifact, UniversalPlugin)
    .settings(scalariformSettings)
    .settings(basicSettings)
    .settings(checkJavaVersion)
    .settings(
        resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
        libraryDependencies ++= Seq(
        ws,
        "com.amazonaws" % "aws-java-sdk-kinesis" % "1.10.45",
        "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.10.45",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
        "io.megl" % "play-json-extra_2.11" % "2.4.3",
        "org.scalactic" %% "scalactic" % "2.2.6",
        "com.gu" %% "play-googleauth" % "0.6.0",
        "com.gu" %% "scanamo" % "0.1.0",
        "org.scalatest" %% "scalatest" % "2.2.5" % "test"
      ),

      addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),

      routesGenerator := InjectedRoutesGenerator,
      riffRaffPackageName := "content-api-floodgate",
      riffRaffPackageType := (packageZipTarball in Universal).value
    )

  lazy val checkJavaVersion = {
    initialize := {
      val _ = initialize.value
      assert(sys.props("java.specification.version") == "1.8",
        "Java 8 is required for this project.")
    }
  }
}
