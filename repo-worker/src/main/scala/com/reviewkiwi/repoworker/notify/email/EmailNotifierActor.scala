package com.reviewkiwi.repoworker.notify.email

import akka.actor._
import com.reviewkiwi.repoworker.fetcher.NewCommit
import org.eclipse.jgit.api.Git
import com.reviewkiwi.repoworker.git.GitDiffer
import org.eclipse.jgit.revwalk.RevCommit
import com.reviewkiwi.repoworker.notify.template.html.LineByLineDiffEmailHtml
import com.reviewkiwi.model.{ChangeFetched, ChangeToFetch}
import com.weiglewilczek.slf4s.Logging

class EmailNotifierActor(differ: GitDiffer, htmlReporter: LineByLineDiffEmailHtml, reportSender: ActorRef)
  extends Actor with Logging {

  def receive = {
    case NewCommit(revCommit, repoDir) =>
      val git = Git.open(repoDir)

      if (alreadyNotified(git, revCommit)) {
        //
      }

      logger.info("[%s] by [%s]: %s".format(revCommit.getName, revCommit.getAuthorIdent.getName, revCommit.getShortMessage))
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
      markAsNotified(git, revCommit)

  }

  def alreadyNotified(git: Git, commit: RevCommit): Boolean = {
    val repoName = getRepoName(git)
    ChangeFetched.alreadyNotifiedAbout(repoName, commit)
  }

  // todo should also match on the repository name...
  def deleteChangeToFetch(commit: RevCommit) = {
    import com.foursquare.rogue.Rogue._
    ChangeToFetch.where(_.objectId eqs commit.getName).findAndDeleteOne()
  }

  def markAsNotified(git: Git, commit: RevCommit) = {
    val repoName = getRepoName(git)

    logger.info("Marking [%s] in [%s] as Notified".format(commit.getName, repoName))
    ChangeFetched.markAsAlreadyNotifiedAbout(repoName, commit)
  }

  // todo triplicated
  private def getRepoName(git: Git): String = {
    val config = git.getRepository.getConfig
    config.load()
    val name = config.getString("remote", "origin", "url") match {
      case LineByLineDiffEmailHtml.GitHubUrl(repoName) => repoName
      case otherUrl => throw new Exception("Only github repos are supported currently... Can't use [%s] ".format(otherUrl))
    }

    name
  }

}
