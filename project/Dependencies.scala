import sbt._
import Keys._

object Dependencies {
  // Versions
  val http4sVersion = "0.21.19"

  // Libraries
  val pi4s = "com.dyercode" %% "pi4j-sw" % "0.1-SNAPSHOT"
  val catsEffect = "org.typelevel" %% "cats-effect" % "2.3.3"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.5"
  val munit = "org.scalameta" %% "munit" % "0.7.22"
  val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
  val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % http4sVersion
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.14.0"

  // Projects
  val iolightsDependencies: Seq[ModuleID] = Seq(
    pi4s,
    catsEffect,
    http4sDsl,
    http4sBlazeServer,
    pureconfig,
    scalaTest % Test,
    munit % Test
  )
}
