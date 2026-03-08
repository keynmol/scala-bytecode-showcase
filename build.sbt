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

val snapshotDiff = inputKey[Unit]("Generate diff between two version snapshots: snapshotDiff <fromVersion> <toVersion> <kind>")

snapshotDiff := {
  import complete.DefaultParsers._
  import scala.sys.process._

  val args = spaceDelimited("<arg>").parsed
  if (args.size != 3) {
    sys.error("Usage: snapshotDiff <fromVersion> <toVersion> <kind (decompiled|javap)>")
  }
  val Seq(fromVersion, toVersion, kind) = args
  if (kind != "decompiled" && kind != "javap") {
    sys.error(s"Invalid kind '$kind'. Must be 'decompiled' or 'javap'")
  }

  val snapshotsDir = baseDirectory.value / "snapshots"
  val fromDir = snapshotsDir / fromVersion
  val toDir = snapshotsDir / toVersion

  if (!fromDir.exists()) sys.error(s"Snapshot directory not found: $fromDir")
  if (!toDir.exists()) sys.error(s"Snapshot directory not found: $toDir")

  val outputFile = snapshotsDir / s"${fromVersion}_${toVersion}_$kind.diff"

  // Find matching files in the 'from' directory
  val fromFiles = fromDir.listFiles().filter(_.getName.endsWith(s"_$kind")).sortBy(_.getName)

  val diffOutput = new StringBuilder
  for (fromFile <- fromFiles) {
    val toFile = toDir / fromFile.getName
    if (toFile.exists()) {
      val cmd = Seq("git", "diff", "--no-index", fromFile.getAbsolutePath, toFile.getAbsolutePath)
      val output = new StringBuilder
      // git diff returns 1 when there are differences, so we ignore the exit code
      cmd.!(ProcessLogger(line => output.append(line + "\n"), _ => ()))
      diffOutput.append(output)
    }
  }

  IO.write(outputFile, diffOutput.toString)
  println(s"Diff written to: $outputFile")
}
