package compiler.scala3

import compiler.iface.*
import dotty.tools.dotc.{Driver, Compiler}
import dotty.tools.dotc.core.Contexts.{Context, ContextBase}
import dotty.tools.dotc.reporting.{Reporter, Diagnostic}
import dotty.tools.dotc.util.SourceFile
import dotty.tools.dotc.interfaces
import dotty.tools.io.{VirtualFile, AbstractFile}

import java.io.File
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.*
import scala.compiletime.uninitialized

class Scala3CompilationError(
    val _line: Int,
    val _column: Int,
    val _message: String
) extends CompilationError:
  override def line(): Int = _line
  override def column(): Int = _column
  override def message(): String = _message
end Scala3CompilationError

class Scala3CompilationResult(
    val _success: Boolean,
    val _errors: Array[CompilationError],
    val _classFiles: Array[String]
) extends CompilationResult:
  override def success(): Boolean = _success
  override def errors(): Array[CompilationError] = _errors
  override def classFiles(): Array[String] = _classFiles
end Scala3CompilationResult

class AccumulatingReporter extends Reporter:
  private val errors = List.newBuilder[Scala3CompilationError]

  override def doReport(dia: Diagnostic)(using Context): Unit =
    if dia.level == interfaces.Diagnostic.ERROR then
      errors.addOne(
        Scala3CompilationError(dia.pos.line, dia.pos.column, dia.message)
      )

  def getErrors: Array[CompilationError] = errors.result().toArray
end AccumulatingReporter

class CompilerDriver extends Driver:
  private var myInitCtx: Context = uninitialized

  override def sourcesRequired: Boolean = false

  def initialize(args: Array[String]): Unit =
    val ctx = initCtx.fresh
    val summary = setup(args, ctx)
    myInitCtx = summary match
      case Some((_, ctx)) => ctx
      case None           => ctx
  end initialize

  def context: Context = myInitCtx
end CompilerDriver

class Scala3Compiler extends CompilerInterface:
  private var classpath: Array[String] = Array.empty
  private var lazyDriver: Option[CompilerDriver] = None

  override def withClasspath(cp: Array[String]): CompilerInterface =
    val newCompiler = Scala3Compiler()
    newCompiler.classpath = cp
    newCompiler.lazyDriver = None
    newCompiler
  end withClasspath

  private def getDriver: CompilerDriver =
    lazyDriver.getOrElse {
      val driver = CompilerDriver()
      val cpStr = classpath.mkString(File.pathSeparator)
      val args = Array(
        "-classpath",
        cpStr,
        "-color:never",
        "-unchecked",
        "-deprecation"
      )
      driver.initialize(args)
      lazyDriver = Some(driver)
      driver
    }

  override def compile(
      fileName: String,
      contents: String,
      outDir: String
  ): CompilationResult =
    val driver = getDriver
    val reporter = AccumulatingReporter()

    val outPath = Paths.get(outDir)
    if !Files.exists(outPath) then Files.createDirectories(outPath)

    given ctx: Context = driver.context.fresh
      .setReporter(reporter)
      .setSetting(
        driver.context.settings.outputDir,
        AbstractFile.getDirectory(outDir)
      )

    // Clean up existing class files
    if Files.exists(outPath) then
      Files
        .walk(outPath)
        .filter(p => p.toString.endsWith(".class"))
        .forEach(p => Files.deleteIfExists(p))
    end if

    val virtualFile = VirtualFile(fileName, contents.getBytes("UTF-8"))
    val sourceFile = SourceFile(virtualFile, scala.io.Codec.UTF8)

    val compiler = Compiler()
    val run = compiler.newRun
    run.compileSources(List(sourceFile))

    val classFiles =
      if Files.exists(outPath) then
        Files
          .walk(outPath)
          .filter(p => p.toString.endsWith(".class"))
          .map[String](_.toString)
          .toList
          .asScala
          .toArray
      else Array.empty[String]

    Scala3CompilationResult(
      !reporter.hasErrors,
      reporter.getErrors,
      classFiles
    )
  end compile
end Scala3Compiler
