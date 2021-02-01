import sbt._
import Keys._

object Dependencies {
  // Versions

  // Libraries
  val pi4s = "com.dyercode" %% "pi4j-sw" % "0.1-SNAPSHOT"
  val catsEffect = "org.typelevel" %% "cats-effect" % "2.3.1"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.3"
  val munit = "org.scalameta" %% "munit" % "0.7.21"

  // Projects
  val iolightsDependencies: Seq[ModuleID] = Seq(
    pi4s,
    catsEffect,
    scalaTest % Test,
    munit % Test
  )
}
