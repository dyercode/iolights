import Dependencies._

name := "iolights"

// scalaVersion := "2.13.8"
scalaVersion := "3.5.1"

version := "0.2.3-SNAPSHOT"

libraryDependencies ++= iolightsDependencies

assembly / mainClass := Some("com.dyercode.iolights.Main")
