import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader.Systemd
enablePlugins(JavaServerAppPackaging, SystemdPlugin, PlayScala)
disablePlugins(PlayNettyServer)

name := "content-api-floodgate"
organization := "com.gu"
description := "The Content API reindexing control panel"
scalaVersion := "2.13.18"
scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-release","11")
version := "1.0"

resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"

val awsClientVersion = "1.12.671"
val prometheusVersion = "0.16.0"
val PekkoVersion = "1.0.2"

libraryDependencies ++= Seq(
  ws,
  "com.amazonaws"              % "aws-java-sdk-kinesis"  % awsClientVersion,
  "com.amazonaws"              % "aws-java-sdk-dynamodb" % awsClientVersion,
  "com.typesafe.play"          %% "play-json-joda"       % "2.10.3",
  "com.gu.play-googleauth"     %% "play-v30"             % "6.1.0",
  "org.scanamo"                %% "scanamo"              % "1.0.0-M11",
  "org.scanamo"                %% "scanamo-joda"         % "1.0.0-M11",
  "org.typelevel"              %% "cats-core"            % "2.9.0",
  "net.logstash.logback" % "logstash-logback-encoder" % "7.3",
  "io.prometheus" % "simpleclient" % prometheusVersion,
  "io.prometheus" % "simpleclient_hotspot" % prometheusVersion,
  "io.prometheus" % "simpleclient_common" % prometheusVersion,

  "org.apache.pekko" %% "pekko-actor" % PekkoVersion,

  //required to make jackson work
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2",

  "org.playframework" %% "play-specs2" % "3.0.2" % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.apache.pekko" %% "pekko-testkit" % PekkoVersion % Test
)

routesGenerator := InjectedRoutesGenerator

Universal / packageName := "floodgate"
maintainer := "Guardian Content Platforms <content-platforms.dev@theguardian.com>"

Debian / serverLoading := Some(Systemd)
Debian / daemonUser := "content-api"
Debian / daemonGroup := "content-api"
Debian / serviceAutostart := false  //we don't want to start immediately after installation, we want to customise the setup first

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", sys.env.getOrElse("SBT_JUNIT_OUTPUT", "junit"))
