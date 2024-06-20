import sbt._
import Keys._

object Dependencies {
  object Versions {
    val http4s = "0.23.27"
    val munit = "1.0.0"
  }

  val pi4s = "com.dyercode" %% "pi4j-sw" % "0.1.2-SNAPSHOT"
  val catsEffect = "org.typelevel" %% "cats-effect" % "3.5.4"
  val munit = "org.scalameta" %% "munit" % Versions.munit
  val munitCheck = "org.scalameta" %% "munit-scalacheck" % Versions.munit
  val http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.http4s
  val http4sEmberServer =
    "org.http4s" %% "http4s-ember-server" % Versions.http4s
  val pureconfig = ("com.github.pureconfig" %% "pureconfig-core" % "0.17.7")
  val csvReader = "com.github.tototoshi" %% "scala-csv" % "1.3.10"

  val iolightsDependencies: Seq[ModuleID] = Seq(
    pi4s,
    catsEffect,
    http4sDsl,
    http4sEmberServer,
    csvReader,
    pureconfig,
    munit % Test,
    munitCheck % Test,
  )
}
