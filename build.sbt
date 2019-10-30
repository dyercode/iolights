import Dependencies._

name := "iolights"

scalaVersion := "2.13.1"

version := "0.1"

libraryDependencies ++= iolightsDependencies

mainClass in assembly := Some("com.dyercode.iolights.Main")
