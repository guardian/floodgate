name := "content-api-floodgate"
organization := "com.gu"
description := "The Content API reindexing control panel"
scalaVersion := "2.12.8"
scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-target:jvm-1.8")

enablePlugins(PlayScala, RiffRaffArtifact)

resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"

val awsClientVersion = "1.12.332"

libraryDependencies ++= Seq(
  ws,
  "com.amazonaws"              % "aws-java-sdk-kinesis"  % awsClientVersion,
  "com.amazonaws"              % "aws-java-sdk-dynamodb" % awsClientVersion,
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.5",
  "com.typesafe.play"          %% "play-json"            % "2.9.3",
  "com.typesafe.play"          %% "play-json-joda"       % "2.9.3",
  "com.typesafe.play"          %% "play-logback"         % "2.8.18",
  "com.typesafe.play"          %% "play-specs2"          % "2.8.18",
  "com.gu"                     %% "play-googleauth"      % "0.7.7",
  "org.scanamo"                %% "scanamo"              % "1.0.0-M10",
  "org.scanamo"                %% "scanamo-joda"         % "1.0.0-M10",
  "org.scalatest"              %% "scalatest"            % "3.0.4" % "test",
  "org.typelevel"              %% "cats-core"            % "1.6.1",

  //required to make jackson work
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.7"
)

routesGenerator := InjectedRoutesGenerator

riffRaffPackageType := (packageZipTarball in Universal).value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffManifestProjectName := "Content Platforms::floodgate"

