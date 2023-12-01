addSbtPlugin("com.typesafe.play" % "sbt-plugin"            % "2.8.21")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"          % "2.0.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-twirl"             % "1.5.1")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")

//this is required because scala compiler depends on a higher version of scala-xml than sbt-native-packager dependencies
libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
