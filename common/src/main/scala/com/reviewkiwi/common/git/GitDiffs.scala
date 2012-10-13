package com.reviewkiwi.common.git

import org.eclipse.jgit.diff.{DiffFormatter, DiffEntry}
import java.io.ByteArrayOutputStream
import org.eclipse.jgit.lib.Repository
import xml.Elem
import scalaz.Scalaz._
import com.reviewkiwi.common.css.CssStyles

trait GitDiffs {

  case class NumberedDiffEntryLine(line: String, lineNumber: Int)

  implicit def presentDiffEntry(d: DiffEntry)(implicit repo: Repository) = new DiffEntryPresenter(d)(repo)

  class DiffEntryPresenter(diff: DiffEntry)(repo: Repository) {

    val InfoLine = """@@ -(\d+),(\d+) \+(\d+),(\d+) @@""".r

    def asDiffHTML: String = {
      asDiffString
        .split("\n")
        .drop(4)
        .map(l => asDiffNode(l).toString )
        .mkString("\n")
    }

    def asNumberedDiffLines: List[NumberedDiffEntryLine] = {
      val lines = asDiffString.split("\n").drop(5).toList
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

    private def asDiffNode(line: String): Elem = {
      if(line.size < 2) return <pre></pre>
      val realLine = line.drop(1) match {
        case l if line.trim.isEmpty => "&nbsp;"
        case l => l
      }

      line.head match {
        case '+' =>
          <pre style={CssStyles.insertLine}>{realLine}</pre>

        case '-' =>
          <pre style={CssStyles.deleteLine}>{realLine}</pre>

        case _ if InfoLine.pattern.matcher(line).matches() =>
          <pre class={CssStyles.infoLine}>{line}</pre>

        case _ =>
          <pre style={CssStyles.normalLine}>{realLine}</pre>
      }
    }
  }

}
