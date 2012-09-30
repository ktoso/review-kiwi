package com.reviewkiwi.repoworker.notify.template.html

import org.eclipse.jgit.diff.DiffEntry
import io._
import org.eclipse.jgit.api.Git
import com.reviewkiwi.common.git.{GitDiffs, GitObjects, GitWalks}
import org.eclipse.jgit.revwalk.RevCommit
import com.reviewkiwi.common.gravatar.Gravatar
import com.reviewkiwi.repoworker.notify.template.css.CssImages
import org.apache.commons.io.FilenameUtils
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime

class LineByLineDiffEmailHtml extends HtmlReport
  with Gravatar
  with GitWalks with GitObjects with GitDiffs {

  val EmailTemplate = Source.fromInputStream(getClass.getResourceAsStream("/template/email/email.html")).getLines().mkString("\n")
  val EmailFileTemplate = Source.fromInputStream(getClass.getResourceAsStream("/template/email/file.html")).getLines().mkString("\n")

  def build(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]) = {
    val fileDiffs = diffs map { diff => diff2html(git, commit, diff) }

    val commitMessageLines = commit.getFullMessage.split("\n")
    EmailTemplate
      .replace("{{authorGravatarUrl}}", getSmallGravatarUrl(commit.getAuthorIdent.getEmailAddress))
      .replace("{{authorName}}", commit.getAuthorIdent.getName)
      .replace("{{authorEmail}}", commit.getAuthorIdent.getEmailAddress)
      .replace("{{commitDateTime}}", formatDate(commit.getCommitTime))
      .replace("{{messageFirstLine}}", commitMessageLines.head)
      .replace("{{messageFull}}", commitMessageLines.drop(1).mkString("<br/>"))
      .replace("{{commitIdAbbrev}}", commit.abbreviate(8).name()) // todo use object reader!
      .replace("{{githubRepoName}}", "ktoso/example-repo") // todo the url of the github project...
      .replace("{{githubRepoUrl}}", "http://www.github.com/ktoso") // todo the url of the github project...
      .replace("{{githubCommitUrl}}", "http://www.github.com/ktoso/repo/commit/" + commit.getName) // todo url ot the commit on github
      .replace("{{content}}", fileDiffs.mkString("\n"))
  }

  def diff2html(git: Git, commit: RevCommit, diff: DiffEntry): String = {
    implicit val repo = git.getRepository

    val diffHtml = diff.asDiffHTML

    // todo replace with mustache
    EmailFileTemplate
      .replace("{{fileIcon}}", CssImages.fileImageForFile(diff.getNewPath)) // todo handle renames
      .replace("{{fileName}}", diff.getNewPath) // todo handle renames
      .replace("{{diff}}", diffHtml)
  }

  // Sunday, 2 May 2010 @ 19:34
  val CommitDateFormat = DateTimeFormat.forPattern("EE, dd MMMM YYYY @ HH:mm")
  def formatDate(time: Int): String = {
    CommitDateFormat.print(new DateTime(time))
  }
}
