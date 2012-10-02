package com.reviewkiwi.repoworker.notify.template.html

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.api.Git
import com.reviewkiwi.common.git.{GitDiffs, GitObjects, GitWalks}
import org.eclipse.jgit.revwalk.RevCommit
import com.reviewkiwi.common.gravatar.Gravatar
import com.reviewkiwi.repoworker.notify.template.css.CssImages
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import java.util.Date
import xml._
import com.reviewkiwi.common.util.UniquifyVerb
import com.reviewkiwi.common.css.CssStyles

class LineByLineDiffEmailHtml extends HtmlReport
  with Gravatar with UniquifyVerb
  with GitWalks with GitObjects with GitDiffs {

  val EmailTemplate = io.Source.fromInputStream(getClass.getResourceAsStream("/template/email/email.html")).getLines().mkString("\n")
  val EmailFileTemplate = io.Source.fromInputStream(getClass.getResourceAsStream("/template/email/file.html")).getLines().mkString("\n")

  def build(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]) = {
    val fileDiffs = diffs map { diff => diff2html(git, commit, diff) }

    val commitMessageLines = commit.getFullMessage.split("\n")
    EmailTemplate
      .replaceAll('modifiedFilesListing, generateModifiedFilesListing(commit, diffs))
      .replaceAll('authorGravatarUrl, getSmallGravatarUrl(commit.getAuthorIdent.getEmailAddress))
      .replaceAll('authorName, commit.getAuthorIdent.getName)
      .replaceAll('authorEmail, commit.getAuthorIdent.getEmailAddress)
      .replaceAll('commitDateTime, formatDate(commit.getAuthorIdent.getWhen))
      .replaceAll('messageFirstLine, commitMessageLines.head)
      .replaceAll('messageFull, commitMessageLines.drop(1).mkString("<br/>"))
      .replaceAll('commitIdAbbrev, commit.abbreviate(8).name()) // todo use object reader!
      .replaceAll('githubRepoName, "ktoso/example-repo") // todo the url of the github project...
      .replaceAll('githubRepoUrl, "http://www.github.com/ktoso") // todo the url of the github project...
      .replaceAll('githubCommitUrl, "http://www.github.com/ktoso/repo/commit/" + commit.getName) // todo url ot the commit on github
      .replaceAll('content, fileDiffs.mkString("\n"))
  }

  def generateModifiedFilesListing(commit: RevCommit, diffs: Iterable[DiffEntry]): String = {
    val uniqueByFile = diffs.toList.uniquifyOn(_.getNewPath)
    val nodes = for (diff <- uniqueByFile) yield generateModifiedFileNode(commit, diff)

    <ul>{nodes}</ul>.toString()
  }

  def generateModifiedFileNode(commit: RevCommit, diff: DiffEntry): NodeSeq = {
    val Added = <b>[+]</b> % Attribute("style", Text("color:" + CssStyles.InsertColor), Null)
    val Copied = <b>[+]</b> % Attribute("style", Text("color:" + CssStyles.CopiedColor), Null)
    val Deleted = <b>[-]</b> % Attribute("style", Text("color:" + CssStyles.DeletedColor), Null)

    diff.getChangeType match {
      case DiffEntry.ChangeType.ADD =>
        <li>{Added} Added <b>{diff.getNewPath}</b></li>

      case DiffEntry.ChangeType.COPY =>
        <li>{Copied} Copied <b>{diff.getOldPath}</b> to <b>{diff.getNewPath}</b></li>

      case DiffEntry.ChangeType.DELETE =>
        <li>{Deleted} Deleted <b>{diff.getOldPath}</b></li>

      case DiffEntry.ChangeType.MODIFY =>
        <li><b>[m]</b> Modified <b>{diff.getNewPath}</b></li>

      case DiffEntry.ChangeType.RENAME =>
        <li>{Copied} Renamed {diff.getOldPath} => <b>{diff.getNewPath}</b></li>
    }
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

  def formatDate(time: Date): String = {
    CommitDateFormat.print(new DateTime(time))
  }

  implicit def templateMagic(s: Symbol): String = """\{\{""" + s.name + """\}\}"""

}
