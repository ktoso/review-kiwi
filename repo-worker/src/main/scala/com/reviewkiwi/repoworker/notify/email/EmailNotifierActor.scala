package com.reviewkiwi.repoworker.notify.email

import akka.actor._
import com.reviewkiwi.repoworker.fetcher.NewCommit
import org.eclipse.jgit.api.Git
import com.reviewkiwi.repoworker.git.GitDiffer
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevCommit
import com.reviewkiwi.repoworker.notify.template.html.LineByLineDiffHtmlReportReporter
import net.liftweb.util.Mailer.To

class EmailNotifierActor(differ: GitDiffer, htmlReporter: LineByLineDiffHtmlReportReporter, reportSender: ActorRef) extends Actor {

  def receive = {
    case NewCommit(revCommit, repoDir) =>
      val git = Git.open(repoDir)

      println("[%s] by [%s]: %s".format(revCommit.getName, revCommit.getAuthorIdent.getName, revCommit.getShortMessage))
      val diffs = differ.diffWithParent(git, revCommit)

      val body = htmlReporter.build(git, diffs)
      // foreach zainteresowany
      reportSender ! SendEmail(
        To("ktoso@project13.pl"), // todo hardcoded
        topic = "Commit ["+revCommit.getName+"] by " + revCommit.getAuthorIdent.getName,
        body = body
      )

  }
}
