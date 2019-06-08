name := "content-api-floodgate"
organization := "com.gu"
description := "The Content API reindexing control panel"
scalaVersion := "2.12.8"
scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-target:jvm-1.8")

enablePlugins(PlayScala, RiffRaffArtifact)

resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"

libraryDependencies ++= Seq(
  ws,
  "com.amazonaws" % "aws-java-sdk-kinesis" % "1.11.568",
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.568",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.typesafe.play" %% "play-json" % "2.6.13",
  "com.typesafe.play" %% "play-json-joda" % "2.6.13",
  "com.typesafe.play" %% "play-logback" % "2.6.23",
  "com.typesafe.play" %% "play-specs2" % "2.6.23",
  "com.gu" %% "play-googleauth" % "0.7.7",
  "org.scanamo" %% "scanamo" % "1.0.0-M10",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.typelevel" %% "cats-core" % "1.6.1"
)

routesGenerator := InjectedRoutesGenerator

riffRaffPackageType := (packageZipTarball in Universal).value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffManifestProjectName := "Content Platforms::floodgate"

initialize := {
  val _ = initialize.value
  assert(sys.props("java.specification.version") == "1.8",
    "Java 8 is required for this project.")
}

