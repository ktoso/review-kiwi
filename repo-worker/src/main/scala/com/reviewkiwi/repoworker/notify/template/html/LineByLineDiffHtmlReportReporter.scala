package com.reviewkiwi.repoworker.notify.template.html

import org.eclipse.jgit.diff.DiffEntry
import io._
import org.eclipse.jgit.api.Git
import com.reviewkiwi.common.git.{GitDiffs, GitObjects, GitWalks}
import org.eclipse.jgit.revwalk.RevCommit
import com.reviewkiwi.common.gravatar.Gravatar

class LineByLineDiffHtmlReportReporter extends HtmlReport
  with Gravatar
  with GitWalks with GitObjects with GitDiffs {

  val DiffPlaceholder = "{{diff}}"

  val EmailTemplate = Source.fromInputStream(getClass.getResourceAsStream("/template/email/email.html")).getLines().mkString("\n")
  val EmailFileTemplate = Source.fromInputStream(getClass.getResourceAsStream("/template/email/file.html")).getLines().mkString("\n")

  def build(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]) = {
    val fileDiffs = diffs map { diff => diff2html(git, commit, diff) }

    EmailTemplate
      .replace("{{authorGravatarUrl}}", getSmallGravatarUrl(commit.getAuthorIdent.getEmailAddress))
      .replace("{{authorName}}", commit.getAuthorIdent.getName)
      .replace("{{authorEmail}}", commit.getAuthorIdent.getEmailAddress)
      .replace("{{commitMessage}}", commit.getFullMessage)
      .replace(DiffPlaceholder, fileDiffs.mkString("\n"))
  }

  def diff2html(git: Git, commit: RevCommit, diff: DiffEntry): String = {
    implicit val repo = git.getRepository

    val diffHtml = diff.asDiffHTML

    // todo replace with mustache
    EmailFileTemplate
      .replace("{{fileName}}", diff.getNewPath) // todo handle renames
      .replace(DiffPlaceholder, diffHtml)
  }
}
