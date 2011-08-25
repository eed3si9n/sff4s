import sbt._

object Builds extends Build {
  import Keys._
  
	lazy val root = Project("root", file(".")) aggregate(api)
	lazy val api = Project("sff4s-api", file("sff4s-api"))
	lazy val actors = Project("sff4s-actors", file("sff4s-actors")) dependsOn(api % "compile;test->test")
	
  override lazy val settings = super.settings ++ Seq(
    version := "0.1.0-SNAPSHOT",
    organization := "com.eed3si9n",
    scalaVersion := "2.9.0-1",
    crossScalaVersions := Seq("2.9.0-1", "2.8.1"),
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2" % "1.5" % "test"
    ),
    publishArtifact in (Compile, packageBin) := true,
    publishArtifact in (Test, packageBin) := false,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,
    resolvers += ScalaToolsSnapshots,
    publishTo <<= version { (v: String) =>
      val nexus = "http://nexus.scala-tools.org/content/repositories/"
      if(v endsWith "-SNAPSHOT") Some("Scala Tools Nexus" at nexus + "snapshots/")
      else Some("Scala Tools Nexus" at nexus + "releases/")
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  )
}
