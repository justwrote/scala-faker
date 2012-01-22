import sbt._
import Keys._
import sbtrelease.Release._
import ls.Plugin.LsKeys

object Dependencies {

  val snakeYAML = "org.yaml" % "snakeyaml" % "1.9"

  def scalatest(version: String) = {
    def st(scalaVersion: String, projectVersion: String) =
      "org.scalatest" % ("scalatest_" + scalaVersion) % projectVersion % "test"

    version match {
      case "2.8.0" => st("2.8.1", "1.5.1") // argh, there is no 2.8.0 scalatest version in any maven repository
      case "2.8.1" | "2.8.2" => st(version, "1.5.1")
      case "2.9.0" | "2.9.0-1" | "2.9.1" => st(version, "1.6.1")
      case _ => sys.error("ScalaTest not supported for scala version %s!" format version)
    }
  }
  
  def coreDeps(version: String) = Seq(snakeYAML)
  def testDeps(version: String) = Seq(scalatest(version))
}

object ScalaFaker extends Build {
  
  val projectName = "scala-faker"
  
  val buildSettings = Defaults.defaultSettings ++ releaseSettings ++ ls.Plugin.lsSettings ++
    Seq(
      sbtPlugin := false,
      organization := "it.justwrote",
      name := projectName,
      scalaVersion := "2.9.1",
      crossScalaVersions := Seq("2.8.0", "2.8.1", "2.8.2", "2.9.0", "2.9.0-1", "2.9.1"),
      publishArtifact in (Compile, packageDoc) := false,
      scalacOptions ++= Seq("-deprecation", "-Xcheckinit", "-encoding", "utf8", "-g:vars", "-unchecked"),
      parallelExecution := true,
      parallelExecution in Test := true,
      publishTo := Option(Resolver.file("repo", file("/home/ds/Projects/justwrote.github.com/releases"))),
      homepage := Some(new java.net.URL("https://github.com/justwrote/scala-faker/")),
      (LsKeys.tags in LsKeys.lsync) := Seq("test", "fake", "faker"),
      (externalResolvers in LsKeys.lsync) := Seq("justwrote" at "http://repo.justwrote.it/releases/"),
      (description in LsKeys.lsync) := "A library for generating fake data.",
      (LsKeys.docsUrl in LsKeys.lsync) := Some(new java.net.URL("https://github.com/justwrote/scala-faker#readme")),
      initialCommands := "import faker._"
    )

  lazy val scalaFaker = Project(
    projectName,
    file("."),
    settings = buildSettings ++ Seq(
      libraryDependencies <<= (scalaVersion, libraryDependencies) {
        (sv, deps) => deps ++ Dependencies.coreDeps(sv) ++ Dependencies.testDeps(sv)
      }
    )
  )
}
