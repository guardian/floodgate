addSbtPlugin("org.playframework" % "sbt-plugin"            % "3.0.2")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"          % "2.0.0")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
addDependencyTreePlugin

//this is required because scala compiler depends on a higher version of scala-xml than sbt-native-packager dependencies

