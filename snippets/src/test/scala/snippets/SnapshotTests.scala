package snippets

import munit.FunSuite
import com.indoorvivants.snapshots.munit_integration.MunitSnapshotsIntegration
import snippets.macros.SnapshotMacros.snapshotCode

import compiler.iface.Snapshot
import java.io.File
import java.net.URLClassLoader
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

trait SnapshotTests extends FunSuite with MunitSnapshotsIntegration:

  private def createCompiler(
      classpath: Array[String]
  ): compiler.iface.CompilerInterface =
    val compilerJars = System
      .getProperty("java.class.path")
      .split(File.pathSeparator)
      .filter(p =>
        p.contains("scala3-compiler") || p.contains("scala3-library") ||
          p.contains("scala-library") || p.contains("tasty")
      )

    val compilerClass = Class.forName("compiler.scala3.Scala3Compiler")
    val compilerInstance = compilerClass
      .getDeclaredConstructor()
      .newInstance()
      .asInstanceOf[compiler.iface.CompilerInterface]

    compilerInstance.withClasspath(classpath ++ compilerJars)

  private def getInterfaceJar: String =
    System
      .getProperty("java.class.path")
      .split(File.pathSeparator)
      .find(_.contains("compiler-interface"))
      .getOrElse(
        throw new RuntimeException("compiler-interface not found in classpath")
      )

  private def runJavap(classFile: Path): String =
    Seq("javap", "-p", "-c", classFile.toString).!!

  private def decompileWithFernflower(classFile: Path): String =
    val tempOut = Files.createTempDirectory("fernflower-out")

    try
      val args = Array(classFile.toString, tempOut.toString)
      org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler.main(args)

      val javaFiles = Files
        .walk(tempOut)
        .filter(p => p.toString.endsWith(".java"))
        .toList
        .asScala

      if javaFiles.nonEmpty then Files.readString(javaFiles.head)
      else "// No decompiled output generated"
    finally
      Files
        .walk(tempOut)
        .sorted(java.util.Comparator.reverseOrder())
        .forEach(p => Files.deleteIfExists(p))

  private def findAnnotatedClasses(outputDir: Path): List[Path] =
    if !Files.exists(outputDir) then return Nil

    Files
      .walk(outputDir)
      .filter(p => p.toString.endsWith(".class"))
      .toList
      .asScala
      .toList
      .filter { classFile =>
        try
          val url = outputDir.toUri.toURL
          val loader = new URLClassLoader(Array(url), getClass.getClassLoader)
          val className = outputDir
            .relativize(classFile)
            .toString
            .replace(".class", "")
            .replace(File.separator, ".")
          val clazz = loader.loadClass(className)

          clazz.isAnnotationPresent(classOf[compiler.iface.Snapshot]) ||
          clazz.getDeclaredMethods.exists(
            _.isAnnotationPresent(classOf[compiler.iface.Snapshot])
          )
        catch case _: Exception => false
      }

  private def buildSnapshotContent(
      snippetName: String,
      code: String,
      annotatedClasses: List[Path],
      outputDir: Path
  ): (String, String) =
    val separator = "=" * 80

    // Build header with the original snippet
    val header = s"""$separator
// Snippet: $snippetName
$separator

$code

"""

    // Build javap section
    val javapContent = annotatedClasses
      .map { classFile =>
        val className = classFile.getFileName.toString.replace(".class", "")
        val javapOutput = runJavap(classFile)
        s"""$separator
// Definition: $className (javap)
$separator

$javapOutput"""
      }
      .mkString("\n")

    // Build decompiled section
    val decompiledContent = annotatedClasses
      .map { classFile =>
        val className = classFile.getFileName.toString.replace(".class", "")
        val decompiledOutput = decompileWithFernflower(classFile)
        s"""$separator
// Definition: $className (decompiled)
$separator

$decompiledOutput"""
      }
      .mkString("\n")

    (header + javapContent, header + decompiledContent)

  protected def compileAndSnapshot(snippetName: String, code: String): Unit =
    val outputDir = Files.createTempDirectory(s"$snippetName-out")
    try
      val comp = createCompiler(Array(getInterfaceJar))
      val result = comp.compile(s"$snippetName.scala", code, outputDir.toString)

      assert(
        result.success(),
        s"Compilation failed: ${result.errors().map(_.message()).mkString(", ")}"
      )

      val annotatedClasses = findAnnotatedClasses(outputDir)
      assert(annotatedClasses.nonEmpty, "No annotated classes found")

      val (javapSnapshot, decompiledSnapshot) =
        buildSnapshotContent(snippetName, code, annotatedClasses, outputDir)

      assertSnapshot(s"${snippetName}_javap", javapSnapshot)
      assertSnapshot(s"${snippetName}_decompiled", decompiledSnapshot)
    finally
      Files
        .walk(outputDir)
        .sorted(java.util.Comparator.reverseOrder())
        .forEach(p => Files.deleteIfExists(p))

  inline def snapshot(inline snippetName: String)(inline code: Any): Unit =
    val (name, codeStr) = snapshotCode(snippetName)(code)
    compileAndSnapshot(name, codeStr)
end SnapshotTests
