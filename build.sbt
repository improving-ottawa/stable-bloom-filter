import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.improving.lib"
ThisBuild / organizationName := "ImprovingOttawa"

lazy val root = (project in file("."))
  .settings(
    name := "stable-bloom-filter",
    libraryDependencies ++= Seq(
     bloomFilter,
      scalaTest % Test)
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
