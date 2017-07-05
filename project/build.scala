import sbt._
import Keys._
import sbtrelease.ReleasePlugin._

object Dependencies {

  val snakeYAML = "org.yaml" % "snakeyaml" % "1.13"

  def scalatest(version: String) = {
    def st(scalaVersion: String, projectVersion: String) =
      "org.scalatest" % ("scalatest_" + scalaVersion) % projectVersion % "test"

    version match {
      case v if v.startsWith("2.10.") => st("2.10", "2.1.6")
      case v if v.startsWith("2.11.") => st("2.11", "2.1.6")
      case v if v.startsWith("2.12.") => st("2.12", "3.0.3")
      case _ => sys.error("ScalaTest not supported for scala version %s!" format version)
    }
  }

  def coreDeps(version: String) = Seq(snakeYAML)
  def testDeps(version: String) = Seq(scalatest(version))
}

object ScalaFaker extends Build {

  def sourceDir(version: String) = version match {
    case v if v.startsWith("2.10.") || v.startsWith("2.11.") || v.startsWith("2.12")=> ""
    case _ => "-2.8.1-2.9"
  }

  def additionalCompilerOptions(version: String): Seq[String] = version match {
    case v if v.startsWith("2.10.") || v.startsWith("2.11.") || v.startsWith("2.12.") => Seq("-feature")
    case _ => Seq.empty
  }

  val projectName = "scala-faker"

  val buildSettings = Defaults.defaultSettings ++ releaseSettings ++
    Seq(
      sbtPlugin := false,
      organization := "it.justwrote",
      name := projectName,
      crossScalaVersions := Seq("2.10.4", "2.11.0", "2.12.2"),
      publishArtifact in (Compile, packageDoc) := false,
      scalacOptions ++= Seq("-deprecation", "-Xcheckinit", "-encoding", "utf8", "-g:vars", "-unchecked", "-optimize"),
      parallelExecution := true,
      parallelExecution in Test := true,
      publishTo <<= version {
        v => Some(Resolver.file("repo", new java.io.File(System.getProperty("user.home"), "/Projects/justwrote.github.com/" + { if (v.trim.endsWith("SNAPSHOT")) "snapshots" else "releases" } )))
      },
      homepage := Some(new java.net.URL("https://github.com/justwrote/scala-faker/")),
      description := "A library for generating fake data.",
      licenses := Seq(("Apache2", new java.net.URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))),
      initialCommands := "import faker._"
    )

  lazy val scalaFaker = Project(
    projectName,
    file("."),
    settings = buildSettings ++ Seq(
      libraryDependencies <<= (scalaVersion, libraryDependencies) {
        (sv, deps) => deps ++ Dependencies.coreDeps(sv) ++ Dependencies.testDeps(sv)
      },
      scalaSource in Compile <<= (scalaVersion, scalaSource in Compile) {
        (sv, source) => file(source.absolutePath + sourceDir(sv))
      },
      scalacOptions <<= (scalaVersion, scalacOptions) {
        (sv, options) => options map (_ ++ additionalCompilerOptions(sv))
      }
    )
  )
}
