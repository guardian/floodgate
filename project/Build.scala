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
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-target:jvm-1.8")
  )

  lazy val project = Project(id = "content-api-floodgate", base = file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(RiffRaffArtifact, UniversalPlugin)
    .settings(scalariformSettings)
    .settings(basicSettings)
    .settings(checkJavaVersion)
    .settings(
      libraryDependencies ++= Seq(
        ws
      ),
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