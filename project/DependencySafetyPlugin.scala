import sbt._
import sbt.Keys._

object DependencySafetyPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val verifyDependencySafety = taskKey[Unit]("Check resolved dependencies for excluded vulnerable artifacts")
  }
  import autoImport._

  override lazy val projectSettings = Seq(
    verifyDependencySafety := {
      val modules = update.value.configurations.flatMap(_.modules).filterNot(_.evicted).map(_.module)
      require(!modules.exists(m => m.organization == "org.lz4" && m.name == "lz4-java"),
        "The unmaintained org.lz4 artifact must not coexist with the maintained fork")
      require(modules.filter(m => m.organization == "at.yawk.lz4" && m.name == "lz4-java")
        .forall(_.revision == "1.11.4"), "Recheck LZ4 security fixes before changing the pinned version")
    },
    Test / test := (Test / test).dependsOn(verifyDependencySafety).value
  )
}
