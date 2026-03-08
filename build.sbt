import sbt.Keys._

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

lazy val scala3Versions = Seq("3.8.2", "3.3.7")

lazy val compilerInterface = project
  .in(file("compiler-interface"))
  .settings(
    name := "compiler-interface",
    crossPaths := false,
    autoScalaLibrary := false
  )

lazy val scala3Compiler = projectMatrix
  .in(file("scala-3-compiler"))
  .jvmPlatform(CrossVersion.full, scala3Versions)
  .dependsOn(compilerInterface)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" %% "scala3-compiler" % scalaVersion.value
    )
  )

lazy val snippets = projectMatrix
  .in(file("snippets"))
  .jvmPlatform(CrossVersion.full, scala3Versions)
  .dependsOn(compilerInterface)
  .dependsOn(scala3Compiler)
  .enablePlugins(SnapshotsPlugin)
  .settings(
    name := "snippets",
    snapshotsPackageName := "snapshots",
    snapshotsIntegrations += SnapshotIntegration.MUnit,
    snapshotsProjectIdentifier := scalaVersion.value,
    snapshotsLocation := (ThisBuild / baseDirectory).value / "snapshots",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.vineflower" % "vineflower" % "1.10.1" % Test
    ),
    Test / fork := true
  )

lazy val root = project
  .in(file("."))
  .aggregate(compilerInterface)
  .aggregate(scala3Compiler.projectRefs *)
  .aggregate(snippets.projectRefs *)
  .settings(
    name := "snapshot-compiler-demo"
  )
