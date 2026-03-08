package snippets.macros

import scala.quoted.*

object SnapshotMacros:

  transparent inline def snapshotCode(inline snippetName: String)(
      inline code: Any
  ): (String, String) =
    ${ snapshotCodeImpl('snippetName, 'code) }

  private def snapshotCodeImpl(snippetName: Expr[String], code: Expr[Any])(
      using Quotes
  ): Expr[(String, String)] =
    import quotes.reflect.*

    def isUserDefinition(stmt: Statement): Boolean =
      stmt match
        case d: DefDef =>
          !d.symbol.flags.is(Flags.Synthetic) &&
          !d.name.contains("$default$")
        case c: ClassDef =>
          !c.symbol.flags.is(Flags.Synthetic)
        case _: Import => true
        case _ => false

    def getStatementPos(stmt: Statement): Position =
      stmt match
        case t: Term     => t.pos
        case d: DefDef   => d.pos
        case c: ClassDef => c.pos
        case v: ValDef   => v.pos
        case i: Import   => i.pos
        case other       => other.pos

    def getStatementSource(stmt: Statement): String =
      val pos = getStatementPos(stmt)
      pos.sourceFile.content
        .map(_.slice(pos.start, pos.end))
        .getOrElse(stmt.show)

    def extractSource(term: Term): String =
      term match
        case Inlined(_, _, inner) => extractSource(inner)
        case Block(stats, expr) =>
          val allParts: List[Statement] = stats :+ expr
          val definitions = allParts.filter(isUserDefinition)
          val uniqueParts = definitions
            .map { stmt =>
              val pos = getStatementPos(stmt)
              ((pos.start, pos.end), stmt)
            }
            .distinctBy(_._1)
            .map(_._2)

          uniqueParts.map(getStatementSource).mkString("\n\n")
        case other =>
          val pos = other.pos
          pos.sourceFile.content
            .map(_.slice(pos.start, pos.end))
            .getOrElse(other.show)

    val codeString = extractSource(code.asTerm)

    // Add necessary import for @Snapshot annotation
    val fullCode = s"import compiler.iface.Snapshot\n\n$codeString"

    '{ ($snippetName, ${ Expr(fullCode) }) }
end SnapshotMacros
