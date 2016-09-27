name := """tictoe.io"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

//BuildInfoPlugin settings
buildInfoKeys := Seq[BuildInfoKey](
  name,
  version,
  "gitBranch" -> git.gitCurrentBranch.value,
  "gitSha" -> git.gitHeadCommit.value.get,
  "buildDate" -> System.currentTimeMillis
)
buildInfoPackage := "buildInfo"
buildInfoOptions += BuildInfoOption.ToJson
