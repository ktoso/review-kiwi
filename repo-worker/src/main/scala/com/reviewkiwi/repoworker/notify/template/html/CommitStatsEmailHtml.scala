package com.reviewkiwi.repoworker.notify.template.html

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.diff.DiffEntry
import com.reviewkiwi.common.git.GitDiffs
import xml.NodeSeq
import com.reviewkiwi.common.css.CssStyles

trait CommitStatsEmailHtml extends HtmlReport
  with GitDiffs {

  override abstract def buildData(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]): Map[String, Any] = {
    val superData = super.buildData(git, commit, diffs)

    val (added, deleted) = generateStats(git, commit, diffs)

    val data = Map(
      "commitStats" -> Map(
        "added" -> added,
        "deleted" -> deleted
      ),
      "commitStatsGraph" -> generateStatsGraphHtml(added, deleted)
    )

    superData ++ data
  }

  def generateStats(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]): (Int, Int) = {
    implicit val repo = git.getRepository

    val it = diffs map { diff =>
      val lines = diff.asDiffString.split("\n")
      (lines.count(_.startsWith("+")), lines.count(_.startsWith("-")))
    }

    it.foldLeft((0, 0)) { case (l,r) => (l._1 + r._1, l._2 + r._2) }
  }

  def generateStatsGraphHtml(added: Long, deleted: Long): NodeSeq = {
    val green = "line-height: 15px; height: 15px; width: 15px; background-color: " + CssStyles.InsertIconColor
    val red = "line-height: 15px; height: 15px; width: 15px; background-color: " + CssStyles.DeletedIconColor

    val addedPercent = added.toDouble / (added + deleted) * 100
    def colorForUnder(upTo: Int) = if (addedPercent > upTo) green else red

    <table cellpadding="0" rowpadding="0" style="padding:0; margin:0">
      <tr>
        <td style={colorForUnder(10)}>&nbsp;</td>
        <td style={colorForUnder(20)}>&nbsp;</td>
        <td style={colorForUnder(30)}>&nbsp;</td>
        <td style={colorForUnder(40)}>&nbsp;</td>
        <td style={colorForUnder(50)}>&nbsp;</td>
        <td style={colorForUnder(60)}>&nbsp;</td>
        <td style={colorForUnder(70)}>&nbsp;</td>
        <td style={colorForUnder(80)}>&nbsp;</td>
        <td style={colorForUnder(90)}>&nbsp;</td>
        <td style={colorForUnder(100)}>&nbsp;</td>
      </tr>
    </table>
  }

  val style =
    """
      |.clear{
      |clear:both;}
      |
      |.graphcont {
      |padding-top:10px;
      |color:#000;
      |font-weight:700;
      |float:left
      |}
      |
      |.graph {
      |float:left;
      |margin-top:10px;
      |background-color:#cecece;
      |position:relative;
      |width:280px;
      |padding:0
      |}
      |
      |.graph .bar {
      |display:block;
      |position:relative;
      |background-image:url(images/bargraph.gif);
      |background-position:right center;
      |background-repeat:repeat-x;
      |border-right:#538e02 1px solid;
      |text-align:center;
      |color:#fff;
      |height:25px;
      |font-family:Arial, Helvetica, sans-serif;
      |font-size:12px;
      |line-height:1.9em
      |}
      |
      |.graph .bar span {
      |position:absolute;
      |left:1em
      |}
      |
    """.stripMargin


}