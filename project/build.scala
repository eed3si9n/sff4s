import sbt._

object BuildSettings {
  import Keys._
  
  val buildSettings = Defaults.defaultSettings ++ Seq(
    version := "0.1.1",
    organization := "com.eed3si9n",
    scalaVersion := "2.9.1",
    crossScalaVersions := Seq("2.8.1", "2.9.1"),
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
  
	lazy val root = Project("root", file("."), settings = buildSettings ++ Seq(
	    name := "sff4s",
	    publish := {}
	  )) aggregate(api, actors, juc, twitter)
	
	lazy val api = Project("sff4s-api", file("sff4s-api"), settings = buildSettings)
	lazy val actors = Project("sff4s-actors", file("sff4s-actors"), settings = buildSettings) dependsOn(api % "compile;test->test")
	lazy val juc = Project("sff4s-juc", file("sff4s-juc"), settings = buildSettings) dependsOn(api % "compile;test->test")
	
	lazy val akka = Project("sff4s-akka", file("sff4s-akka"),
	  settings = buildSettings ++ Seq(
	    resolvers += "Akka Repo" at "http://akka.io/repository",
	    libraryDependencies <+= (scalaVersion) { (sv) => sv match {
	      case "2.9.1" => "se.scalablesolutions.akka" % "akka-actor" % "1.2"
	      case "2.8.1" => "se.scalablesolutions.akka" % "akka-actor" % "1.0"
	    }}
	  )) dependsOn(api % "compile;test->test")
	  
	lazy val twitter = Project("sff4s-twitter-util", file("sff4s-twitter-util"),
	  settings = buildSettings ++ Seq(
	    resolvers += "twttr.com Repo" at "http://maven.twttr.com",
	    libraryDependencies <+= (scalaVersion) { (sv) => sv match {
	      case "2.9.1" => "com.twitter" %% "util-core" % "1.12.8"
	      case "2.8.1" => "com.twitter" % "util-core" % "1.12.7"
	    }}
	  )) dependsOn(api % "compile;test->test")
}
