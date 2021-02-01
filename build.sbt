import Dependencies._

name := "iolights"

scalaVersion := "2.13.4"

version := "0.2"

libraryDependencies ++= iolightsDependencies

testFrameworks += new TestFramework("munit.Framework")

mainClass in assembly := Some("com.dyercode.iolights.Main")
