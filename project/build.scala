import sbt._

object BuildSettings {
  import Keys._
  
  val buildSettings = Defaults.defaultSettings ++ Seq(
    version := "0.1.0-SNAPSHOT",
    organization := "com.eed3si9n",
    scalaVersion := "2.8.1",
    crossScalaVersions := Seq("2.9.0-1", "2.8.1"),
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2" % "1.5" % "test"
    ),
    scalacOptions += "-unchecked",
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

object Builds extends Build {
  import Keys._
  import BuildSettings._
  
	lazy val root = Project("root", file("."), settings = buildSettings) aggregate(api, actors, twitter)
	
	lazy val api = Project("sff4s-api", file("sff4s-api"), settings = buildSettings)
	lazy val actors = Project("sff4s-actors", file("sff4s-actors"), settings = buildSettings) dependsOn(api % "compile;test->test")
	lazy val twitter = Project("sff4s-twitter-util", file("sff4s-twitter-util"),
	  settings = buildSettings ++ Seq(
	    resolvers += "twttr.com Repo" at "http://maven.twttr.com",
	    libraryDependencies += "com.twitter" % "util-core" % "1.11.4"
	  )) dependsOn(api % "compile;test->test")
}
