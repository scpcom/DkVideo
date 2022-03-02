// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.github.scpcom"

val chiselVersion = "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "DkVideo",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "0.5.1" % "test",
      "org.armadeus" %% "fpgamacro" % "0.1.0",
      "com.ovhcloud" %% "sv2chisel-helpers" % "0.5.0",
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-P:chiselplugin:genBundleElements",
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
    resolvers ++= Seq(
      "New Sonatype Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/",
      "New Sonatype Releases" at "https://s01.oss.sonatype.org/service/local/repositories/releases/content/",
    ),
  )

