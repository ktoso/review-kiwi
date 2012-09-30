package com.reviewkiwi.common.git

import org.eclipse.jgit.diff.{DiffFormatter, DiffEntry}
import java.io.ByteArrayOutputStream
import org.eclipse.jgit.lib.Repository
import xml.Elem

trait GitDiffs {

  implicit def presentDiffEntry(d: DiffEntry)(implicit repo: Repository) = new DiffEntryPresenter(d)(repo)

  class DiffEntryPresenter(diff: DiffEntry)(repo: Repository) {

    def asDiffHTML: String = {
      asDiffString
        .split("\n")
        .drop(4)
        .map(l => asDiffNode(l).toString )
        .mkString("\n")
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
      val realLine = line.drop(1)

      line.head match {
        case ' ' => <pre>{realLine}</pre>
        case '+' => <pre style="insert">{realLine}</pre>
        case '-' => <pre style="delete">{realLine}</pre>
        case  _  => <pre style="info">{realLine}</pre>
      }
    }
  }

}