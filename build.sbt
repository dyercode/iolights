import Dependencies._

name := "iolights"

scalaVersion := "2.13.6"

version := "0.2"

libraryDependencies ++= iolightsDependencies

assembly / mainClass := Some("com.dyercode.iolights.Main")
