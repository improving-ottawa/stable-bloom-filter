import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.15" % Test
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.17.0" % Test
}
