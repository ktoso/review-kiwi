package com.reviewkiwi.repoworker.notify.email

import akka.actor._
import com.reviewkiwi.repoworker.fetcher.NewCommit
import org.eclipse.jgit.api.Git
import com.reviewkiwi.repoworker.git.GitDiffer
import org.eclipse.jgit.revwalk.RevCommit
import com.reviewkiwi.repoworker.notify.template.html.LineByLineDiffEmailHtml
import com.reviewkiwi.model.ChangeToFetch

class EmailNotifierActor(differ: GitDiffer, htmlReporter: LineByLineDiffEmailHtml, reportSender: ActorRef) extends Actor {

  def receive = {
    case NewCommit(revCommit, repoDir) =>
      val git = Git.open(repoDir)

      println("[%s] by [%s]: %s".format(revCommit.getName, revCommit.getAuthorIdent.getName, revCommit.getShortMessage))
      val diffs = differ.diffWithParent(git, revCommit)

      val body = htmlReporter.build(git, revCommit, diffs)

      reportSender ! SendEmail(
        "konrad.malawski@softwaremill.pl", // todo hardcoded
        topic = revCommit.getAuthorIdent.getName + " pushed [" + revCommit.getFullMessage.split("\n").head + "]",
        body = body,
        replyTo = Some(revCommit.getAuthorIdent.getEmailAddress)
      )

      // mark as resolved, delete the request
      deleteChangeToFetch(revCommit)

  }

  // todo should also match on the repository name...
  def deleteChangeToFetch(commit: RevCommit) = {
    import com.foursquare.rogue.Rogue._
    ChangeToFetch.where(_.objectId eqs commit.getName).findAndDeleteOne()
  }
}
