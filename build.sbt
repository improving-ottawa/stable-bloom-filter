import Dependencies.*

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.improving.lib"
ThisBuild / organizationName := "ImprovingOttawa"
ThisBuild / organizationHomepage := Some(url("https://improving.com/location/ottawa/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/improving-ottawa/stable-bloom-filter"),
    "scm:git@github.com:improving-ottawa/stable-bloom-filter.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "MZainalpour",
    name = "Mohsen Zainalpour",
    email = "mohsen.zainalpour@improving.com",
    url = url("https://www.linkedin.com/in/zainalpour/")
  )
)

ThisBuild / description := "An implementation of a Stable Bloom Filter for filtering duplicates out of data streams."
ThisBuild / licenses := List(
  "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
)
ThisBuild / homepage := Some(url("https://github.com/improving-ottawa/stable-bloom-filter"))

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

lazy val root = (project in file("."))
  .settings(
    name := "stable-bloom-filter",
    libraryDependencies ++= Seq(scalaTest,scalaCheck)
  )