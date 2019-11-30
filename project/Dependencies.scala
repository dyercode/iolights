import sbt._
import Keys._

object Dependencies {
  // Versions

  // Libraries
  val pi4s = "com.dyercode" %% "pi4j-se" % "0.1"
  val catsEffect = "org.typelevel" %% "cats-effect" % "2.0.0"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

  // Projects
  val iolightsDependencies: Seq[ModuleID] = Seq(
    pi4s,
    catsEffect,
    scalaTest % Test
  )
}
