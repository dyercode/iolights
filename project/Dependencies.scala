import sbt._
import Keys._

object Dependencies {
  object Versions {
    val http4s = "0.23.11"
    val munit = "0.7.29"
  }

  val pi4s = "com.dyercode" %% "pi4j-sw" % "0.1.1-SNAPSHOT"
  val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.11"
  val munit = "org.scalameta" %% "munit" % Versions.munit
  val munitCheck = "org.scalameta" %% "munit-scalacheck" % Versions.munit
  val http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.http4s
  val http4sBlazeServer =
    "org.http4s" %% "http4s-blaze-server" % Versions.http4s
  val http4sEmberServer =
    "org.http4s" %% "http4s-ember-server" % Versions.http4s
  val pureconfig = ("com.github.pureconfig" %% "pureconfig" % "0.17.1").cross(
    CrossVersion.for3Use2_13
  )
  val csvReader = "com.github.tototoshi" %% "scala-csv" % "1.3.10"
  val config = "com.typesafe" % "config" % "1.4.2"

  val iolightsDependencies: Seq[ModuleID] = Seq(
    pi4s,
    catsEffect,
    http4sDsl,
    http4sEmberServer,
    config,
    csvReader,
    munit % Test,
    munitCheck % Test,
  )
}
