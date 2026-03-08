import sbt.Keys._

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

lazy val scala3Version = "3.8.2"

lazy val compilerInterface = project
  .in(file("compiler-interface"))
  .settings(
    name := "compiler-interface",
    crossPaths := false,
    autoScalaLibrary := false
  )

lazy val scala3Compiler = project
  .in(file("scala-3-compiler"))
  .dependsOn(compilerInterface)
  .settings(
    name := "scala-3-compiler",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scala-lang" %% "scala3-compiler" % scala3Version
    )
  )

lazy val snippets = project
  .in(file("snippets"))
  .dependsOn(compilerInterface, scala3Compiler)
  .enablePlugins(SnapshotsPlugin)
  .settings(
    name := "snippets",
    scalaVersion := scala3Version,
    snapshotsPackageName := "snapshots",
    snapshotsIntegrations += SnapshotIntegration.MUnit,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.vineflower" % "vineflower" % "1.10.1" % Test
    ),
    Test / fork := true
  )

lazy val root = project
  .in(file("."))
  .aggregate(compilerInterface, scala3Compiler, snippets)
  .settings(
    name := "snapshot-compiler-demo"
  )
