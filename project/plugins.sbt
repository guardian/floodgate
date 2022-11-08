addSbtPlugin("com.typesafe.play" % "sbt-plugin"            % "2.8.18")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"          % "2.0.0")
addSbtPlugin("com.gu"            % "sbt-riffraff-artifact" % "1.1.9")
addSbtPlugin("com.typesafe.sbt"  % "sbt-twirl"             % "1.5.1")

//this is required because scala compiler depends on a higher version of scala-xml than rifffraff-artifact's dependencies
libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)