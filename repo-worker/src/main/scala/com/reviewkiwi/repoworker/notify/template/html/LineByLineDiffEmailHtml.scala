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
import util.matching.Regex
import org.fusesource.scalate.{Binding, TemplateEngine}
import org.apache.commons.io.FilenameUtils

class LineByLineDiffEmailHtml extends HtmlReport
  with Gravatar with UniquifyVerb
  with GitWalks with GitObjects with GitDiffs
  with HighlightInterestingLinesEmailHtml {

  val engine = new TemplateEngine

  def renderEmail(attrs: Map[String, Any] = Map(), bindings: Iterable[Binding] = Nil) =
    engine.layout("/template/email/email.mustache", attrs, bindings)

  val EmailTemplate = io.Source.fromInputStream(getClass.getResourceAsStream("/template/email/email.html")).getLines().mkString("\n")
  val EmailFileTemplate = io.Source.fromInputStream(getClass.getResourceAsStream("/template/email/file.html")).getLines().mkString("\n")

  def build(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]) = {
    val data = buildData(git, commit, diffs)
    renderEmail(data)
  }

  override def buildData(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]) = {
    val superData = super.buildData(git, commit, diffs)

    val fileDiffs = diffs map { diff => diff2html(git, commit, diff) }

    val repoName = getRepoName(git)
    val commitUrl = getCommitUrl(repoName, commit.getName)
    val commitMessageLines = commit.getFullMessage.split("\n")
    val authorGravatarUrl = getSmallGravatarUrl(commit.getAuthorIdent.getEmailAddress)
    val prettyCommitDate = formatDate(commit.getAuthorIdent.getWhen)

    val data = Map(
      "modifiedFiles"        -> generateModifiedFilesListing(commit, diffs),
      "authorGravatarUrl"    -> authorGravatarUrl,
      "authorName"           -> commit.getAuthorIdent.getName,
      "authorEmail"          -> commit.getAuthorIdent.getEmailAddress,
      "commitDateTime"       -> prettyCommitDate,
      "messageFirstLine"     -> commitMessageLines.head,
      "messageFull"          -> commitMessageLines.drop(1).mkString("<br/>"),
      "commitIdAbbrev"       -> commit.abbreviate(8).name(), // todo use object reader!
      "githubRepoName"       -> repoName,
      "githubRepoUrl"        -> repoName,
      "githubCommitUrl"      -> commitUrl,
      "diffContents,"        -> fileDiffs.mkString("\n")
    )

    superData ++ data
  }

  def generateModifiedFilesListing(commit: RevCommit, diffs: Iterable[DiffEntry]): List[ModifiedFile] = {
    val uniqueByFile = diffs.toList.uniquifyOn(_.getNewPath)
    val nodes = for (diff <- uniqueByFile) yield generateModifiedFileNode(commit, diff)

    nodes
  }

  def getRepoName(git: Git): String = {
    val config = git.getRepository.getConfig
    config.load()
    val name = config.getString("remote", "origin", "url") match {
      case LineByLineDiffEmailHtml.GitHubUrl(repoName) => repoName
      case otherUrl => throw new Exception("Only github repos are supported currently... Can't use [%s] ".format(otherUrl))
    }

    name
  }

  def getCommitUrl(repoName: String, commitId: String): String =
    """https://github.com/%s/commit/%s""".format(repoName, commitId)

  def getGitHubRepoUrl(repoName: String): String =
    "https://github.com/" + repoName

  case class ModifiedFile(action: String, fullPath: String) {
    val path = FilenameUtils.getPath(fullPath)
    val fileName = FilenameUtils.getBaseName(fullPath)
  }

  def generateModifiedFileNode(commit: RevCommit, diff: DiffEntry): ModifiedFile = {
    val Added = <b>[+]</b> % Attribute("style", Text("color:" + CssStyles.AddedIconColor), Null)
    val Copied = <b>[+]</b> % Attribute("style", Text("color:" + CssStyles.CopiedColor), Null)
    val Deleted = <b>[-]</b> % Attribute("style", Text("color:" + CssStyles.DeletedIconColor), Null)

    diff.getChangeType match {
      case DiffEntry.ChangeType.ADD =>
        ModifiedFile("Added", diff.getNewPath)

      case DiffEntry.ChangeType.COPY =>
        ModifiedFile("Copied", diff.getOldPath + " to " + diff.getNewPath)

      case DiffEntry.ChangeType.DELETE =>
        ModifiedFile("Deleted", diff.getOldPath)

      case DiffEntry.ChangeType.MODIFY =>
        ModifiedFile("Modified", diff.getNewPath)

      case DiffEntry.ChangeType.RENAME =>
         ModifiedFile("Renamed", diff.getOldPath + " => " + diff.getNewPath)
    }
  }

  def diff2html(git: Git, commit: RevCommit, diff: DiffEntry): Map[String, Any] = {
    implicit val repo = git.getRepository

    Map(
      "fileIcon" -> CssImages.fileImageForFile(diff.getNewPath), // todo handle renames
      "fileName" -> diff.getNewPath, // todo handle renames
      "diff"     -> diff.asDiffHTML
    )
  }

  // Sunday, 2 May 2010 @ 19:34
  val CommitDateFormat = DateTimeFormat.forPattern("EE, dd MMMM YYYY @ HH:mm")

  def formatDate(time: Date): String = {
    CommitDateFormat.print(new DateTime(time))
  }

  implicit def templateMagic(s: Symbol): String = """\{\{""" + s.name + """\}\}"""

}

object LineByLineDiffEmailHtml {
  val GitHubUrl: Regex = """https?://github.com/(.*).git""".r

}