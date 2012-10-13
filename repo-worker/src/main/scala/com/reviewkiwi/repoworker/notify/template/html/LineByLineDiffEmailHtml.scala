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
import org.eclipse.jgit.diff.DiffEntry.ChangeType

class LineByLineDiffEmailHtml extends HtmlReport
  with Gravatar with UniquifyVerb
  with GitWalks with GitObjects with GitDiffs
  with HighlightInterestingLinesEmailHtml {

  val engine = new TemplateEngine

  def renderEmail(attrs: Map[String, Any] = Map(), bindings: Iterable[Binding] = Nil) =
    engine.layout("/template/email/email.mustache", attrs, bindings)

  def build(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]) = {
    val data = buildData(git, commit, diffs)
    renderEmail(data)
  }

  override def buildData(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]) = {
    val superData = super.buildData(git, commit, diffs)

    val fileDiffs = diffs map { diff => diffAsDisplayableDiff(git, commit, diff) }

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
      "diffs"                -> fileDiffs
    )

    superData ++ data
  }

  // todo checking if todo detection works
  def generateModifiedFilesListing(commit: RevCommit, diffs: Iterable[DiffEntry]): List[ModifiedFile] = {
    val uniqueByFile = diffs.toList.uniquifyOn(_.getNewPath)
    val nodes = for (diff <- uniqueByFile) yield generateModifiedFileNode(commit, diff)

    nodes.sortBy(_.action)
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

  case class ModifiedFile(changeType: DiffEntry.ChangeType, diff: DiffEntry) {
    val actionIcon = changeType match {
      case DiffEntry.ChangeType.ADD => <b>[+]</b> % Attribute("style", Text("color:" + CssStyles.AddedIconColor), Null)
      case DiffEntry.ChangeType.COPY => <b>[+]</b> % Attribute("style", Text("color:" + CssStyles.CopiedColor), Null)
      case DiffEntry.ChangeType.DELETE => <b>[-]</b> % Attribute("style", Text("color:" + CssStyles.DeletedIconColor), Null)
      case DiffEntry.ChangeType.MODIFY => <b>[+]</b> % Attribute("style", Text("color:" + CssStyles.CopiedColor), Null)
      case DiffEntry.ChangeType.RENAME => <b>[+]</b> % Attribute("style", Text("color:" + CssStyles.CopiedColor), Null)
    }

    val action = changeType match {
      case DiffEntry.ChangeType.ADD => "Added"
      case DiffEntry.ChangeType.COPY => "Copied"
      case DiffEntry.ChangeType.DELETE => "Deleted"
      case DiffEntry.ChangeType.MODIFY => "Modified"
      case DiffEntry.ChangeType.RENAME => "Renamed"
    }

    val displayFileName =
      if(changeType == ChangeType.RENAME)
        <span>
          {FilenameUtils.getPath(diff.getOldPath)}<b>{FilenameUtils.getName(diff.getOldPath)}</b>
          ->
          {FilenameUtils.getPath(diff.getNewPath)}<b>{FilenameUtils.getName(diff.getNewPath)}</b>
        </span>
      else if(changeType == ChangeType.DELETE)
        <span style="text-decoration: line-through;">{FilenameUtils.getPath(diff.getOldPath)}<b>{FilenameUtils.getName(diff.getOldPath)}</b></span>
      else
        <span>{FilenameUtils.getPath(diff.getNewPath)}<b>{FilenameUtils.getName(diff.getNewPath)}</b></span>
  }

  def generateModifiedFileNode(commit: RevCommit, diff: DiffEntry): ModifiedFile = {
    ModifiedFile(diff.getChangeType, diff)
  }

  case class DisplayableDiff(fileIcon: String, pathBefore: String, pathAfter: String, diffLines: List[NumberedDiffEntryLine]) {
    val boxTitle =
      if(pathBefore == pathAfter) {
        FilenameUtils.getName(pathBefore)
      } else if(pathBefore == "/dev/null") {
        FilenameUtils.getName(pathAfter)
      } else if(pathAfter == "/dev/null") {
        <span style="text-decoration: line-through">{FilenameUtils.getName(pathBefore)}</span>
      } else {
        """%s => %s""".format(pathBefore, pathAfter)
      }
  }

  def diffAsDisplayableDiff(git: Git, commit: RevCommit, diff: DiffEntry) = {
    implicit val repo = git.getRepository

    DisplayableDiff(
      fileIcon = CssImages.fileImageForFile(diff.getNewPath),
      pathBefore= diff.getOldPath,
      pathAfter = diff.getNewPath,
      diffLines = diff.asNumberedDiffLines
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