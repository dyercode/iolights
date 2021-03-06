import sbt._
import Keys._

object Dependencies {
  // Versions
  val http4sVersion = "0.23.0-RC1"

  // Libraries
  val pi4s = "com.dyercode" %% "pi4j-sw" % "0.1-SNAPSHOT"
  val catsEffect = "org.typelevel" %% "cats-effect" % "3.1.1"
  val munit = "org.scalameta" %% "munit" % "0.7.26"
  val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
  val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % http4sVersion
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.16.0"

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
