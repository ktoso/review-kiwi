package com.reviewkiwi.repoworker.notify.template.html

import org.eclipse.jgit.diff.DiffEntry
import io._
import org.eclipse.jgit.api.Git

class LineByLineDiffHtmlReportReporter extends HtmlReport {

  val DiffPlaceholder = "{{diff}}"

  val EmailTemplate = Source.fromInputStream(getClass.getResourceAsStream("/template/email/email.html")).getLines().mkString("\n")
  val EmailFileTemplate = Source.fromInputStream(getClass.getResourceAsStream("/template/email/file.html")).getLines().mkString("\n")

  def build(git: Git, diffs: Iterable[DiffEntry]) = {
    val fileDiffs = diffs map { diff => diff2html(git, diff) }
    EmailTemplate.replace(DiffPlaceholder, fileDiffs.mkString("\n"))
  }

  def diff2html(git: Git, diff: DiffEntry): String = {
    val changeType = diff.getChangeType match {
      case DiffEntry.ChangeType.ADD => "insert"
      case DiffEntry.ChangeType.DELETE => "delete"
      case DiffEntry.ChangeType.COPY => "info"
      case _ => "info"
    }

    git.getRepository.resolve(diff.getNewId.name())

    val diffLines = <pre class={changeType}>{diff.toString}</pre>.toString()

    EmailFileTemplate
      .replace("{{fileName}}", diff.getNewPath) // todo handle renames
      .replace(DiffPlaceholder, diffLines)
  }
}
