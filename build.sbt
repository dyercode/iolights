import Dependencies._

name := "iolights"

scalaVersion := "2.13.8"

version := "0.2.1"

libraryDependencies ++= iolightsDependencies

assembly / mainClass := Some("com.dyercode.iolights.Main")
