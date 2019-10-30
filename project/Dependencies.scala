import sbt._
import Keys._

object Dependencies {
  // Versions

  // Libraries
  val catsEffect = "org.typelevel" %% "cats-effect" % "2.0.0"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
  val pi4j: ModuleID = "com.pi4j" % "pi4j-core" % "1.2"

  // Projects
  val iolightsDependencies: Seq[ModuleID] = Seq(
    pi4j,
    catsEffect,
    scalaTest % Test
  )
}
