import sbt._
import Keys._

object Dependencies {
  object Versions {
    val http4s = "0.23.7"
  }

  // Libraries
  val pi4s = "com.dyercode" %% "pi4j-sw" % "0.1.1-SNAPSHOT"
  val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.5"
  val munit = "org.scalameta" %% "munit" % "0.7.29"
  val http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.http4s
  val http4sBlazeServer =
    "org.http4s" %% "http4s-blaze-server" % Versions.http4s
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.17.1"

  // Projects
  val iolightsDependencies: Seq[ModuleID] = Seq(
    pi4s,
    catsEffect,
    http4sDsl,
    http4sBlazeServer,
    pureconfig,
    munit % Test
  )
}
