package com.reviewkiwi.repoworker.notify.template.html

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.diff.DiffEntry
import org.apache.commons.io.FilenameUtils
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import com.reviewkiwi.common.git.GitDiffs
import com.reviewkiwi.repoworker.marker.{InterestingLine, InterestingLineExtractor}

trait HighlightInterestingLinesEmailHtml extends HtmlReport
  with GitDiffs {

  def interestingLineExtractors: List[InterestingLineExtractor] = Nil

  override abstract def buildData(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]): Map[String, Any] = {
    val superData = super.buildData(git, commit, diffs)

    val data = Map(
      "interestingLines" -> findInterestingLines(git, commit, diffs)
    )

    superData ++ data
  }

  def findInterestingLines(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]): List[InterestingLine] = {
    implicit val repo = git.getRepository

    val them = diffs filterNot { _.getChangeType == ChangeType.DELETE } map { diff =>
      val fileName = FilenameUtils.getBaseName(diff.getNewPath)

      val lines = diff.asDiffString.split("\n")
      lines.toList.zipWithIndex map { case (line, n) => markInterestingLines(fileName, line, n) }
    }

    them.flatten.flatten.flatten.toList
  }

  def markInterestingLines(fileName: String, line: String, lineNumber: Int): List[Option[InterestingLine]] = {
    interestingLineExtractors map { _.extract(fileName, line, lineNumber) }
  }

}