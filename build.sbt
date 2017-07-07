name := "content-api-floodgate"
organization := "com.gu"
description := "The Content API reindexing control panel"
scalaVersion := "2.11.8"
scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-target:jvm-1.8")

enablePlugins(PlayScala, RiffRaffArtifact, UniversalPlugin)

resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"

libraryDependencies ++= Seq(
  ws,
  "com.amazonaws" % "aws-java-sdk-kinesis" % "1.11.8",
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.8",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "com.typesafe.play" %% "play-json" % "2.6.0",
  "com.typesafe.play" %% "play-json-joda" % "2.6.0",
  "com.gu" %% "play-googleauth" % "0.6.0",
  "com.gu" %% "scanamo" % "0.9.2",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "com.typesafe.play" %% "play-specs2" % "2.6.0",
  "org.typelevel" %% "cats-core" % "0.9.0",
  "com.typesafe.play" %% "play-logback" % "2.6.0"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

routesGenerator := InjectedRoutesGenerator

riffRaffPackageType := (packageZipTarball in Universal).value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffManifestVcsUrl := "git@github.com:guardian/floodgate.git"
riffRaffManifestProjectName := "Content Platforms::floodgate"

initialize := {
  val _ = initialize.value
  assert(sys.props("java.specification.version") == "1.8",
    "Java 8 is required for this project.")
}

scalariformSettings
