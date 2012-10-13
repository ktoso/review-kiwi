package com.reviewkiwi.common.git

import org.eclipse.jgit.diff.{DiffFormatter, DiffEntry}
import java.io.ByteArrayOutputStream
import org.eclipse.jgit.lib.Repository
import xml.{Text, Null, Attribute, Elem}
import scalaz.Scalaz._
import com.reviewkiwi.common.css.CssStyles

trait GitDiffs {

  val InfoLine = """@@ -(\d+),(\d+) \+(\d+),(\d+) @@""".r

  case class NumberedDiffEntryLine(line: String, lineNumber: Int) {
    val html = asDiffLineColor(line)

    val prettyLineNumber: String = if (lineNumber != 0) lineNumber.toString else ""

    val lineBackgroundColor = backgroundColor(line)
  }

  implicit def presentDiffEntry(d: DiffEntry)(implicit repo: Repository) = new DiffEntryPresenter(d)(repo)

  class DiffEntryPresenter(diff: DiffEntry)(repo: Repository) {

    def asNumberedDiffLines: List[NumberedDiffEntryLine] = {
      val lines = asDiffString.split("\n").drop(4).toList
      lines

      var linesBase: Int = 0
      val numberedLines = lines map {
        case line @ InfoLine(from, before, to, added) =>
          linesBase = from.toInt
          NumberedDiffEntryLine(line, 0)

        case line =>
          val numbered = NumberedDiffEntryLine(line, linesBase)
          linesBase += 1
          numbered
      }

      numberedLines
    }

    def lineCssColor(line: String): String =
      line.head match {
        case '+' => CssStyles.insertLine
        case '-' => CssStyles.deleteLine
        case _ if InfoLine.pattern.matcher(line).matches() => CssStyles.infoLine
        case _ => CssStyles.normalLine
      }

    def asDiffString = {
      val baos = new ByteArrayOutputStream()

      val formatter = new DiffFormatter(baos)
      try {
        formatter.setRepository(repo)
        formatter.format(diff)

        baos.toString

      } finally {
        formatter.release()
      }
    }
  }

  private def asDiffLineColor(line: String): Elem = {
    val commonStyle = Attribute("style", Text( """padding: 0; margin: 0"""), Null)

    if(line.size < 2) return <pre></pre> % commonStyle
    val realLine = line.drop(1) match {
      case l if line.trim.isEmpty => "&nbsp;"
      case l => l
    }

    val preLine = line.head match {
      case '+' =>
        <pre>{realLine}</pre>

      case '-' =>
       <pre>{realLine}</pre>

      case _ if InfoLine.pattern.matcher(line).matches() =>
        <pre>{line}</pre>

      case _ =>
        <pre>{realLine}</pre>
    }

    preLine % commonStyle
  }

  private def backgroundColor(line: String): String = {
    if(line.size < 2) return CssStyles.NormalBackgroundColor

    line.head match {
      case '+' => CssStyles.InsertBackgroundColor
      case '-' => CssStyles.DeleteBackgroundColor
      case _ if InfoLine.pattern.matcher(line).matches() => CssStyles.InfoBackgroundColor
      case _ => CssStyles.NormalBackgroundColor
    }
  }

}
