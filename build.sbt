import Dependencies._

name := "iolights"

// scalaVersion := "2.13.8"
scalaVersion := "3.1.3"

version := "0.2.2"

libraryDependencies ++= iolightsDependencies

assembly / mainClass := Some("com.dyercode.iolights.Main")
