package com.reviewkiwi.repoworker.git

import java.io.File
import org.eclipse.jgit.api.Git
import java.lang.Iterable
import org.eclipse.jgit.revwalk.RevCommit
import com.weiglewilczek.slf4s.Logging
import org.eclipse.jgit.lib.ObjectId
import com.reviewkiwi.model.ChangeFetched
import collection.JavaConversions._
import com.reviewkiwi.repoworker.notify.template.html.LineByLineDiffEmailHtml

class FreshCommitsExtractor extends Logging {

  val MaxFreshCommits = 20

  // todo obviously fix ;-)
  def notYetNotifiedAbout(repoDir: File): List[RevCommit] = {
    val git = Git.open(repoDir)

    val commits = getLatestCommitsFromRepo(git)

    commits foreach { commit =>
      val repoName = getRepoName(git)

      val maybeNewCommit = ChangeFetched.createIfNotPersistedYet(repoName, commit)
      maybeNewCommit map { c => logger.debug("Created ChangeFetched [%s] in [%s], objectId: [%s]".format(commit.getName, repoName, c.id)) }
    }

    val withoutAlreadyNotifiedAbout = commits.filterNot(ChangeFetched.alreadyNotifiedAbout).toList

    logger.info("Got [%s] commits from [%s] (before filtering [%s])".format(withoutAlreadyNotifiedAbout.size, repoDir, commits.size))

    withoutAlreadyNotifiedAbout
  }


  def getLatestCommitsFromRepo(git: Git): List[RevCommit] = {
    git.checkout
      .setName("refs/remotes/origin/master")
      .setForce(true)
      .call()

    val commits = git.log
      .setMaxCount(MaxFreshCommits)
      .call().toList
    commits
  }

  // todo duplicated
  private def getRepoName(git: Git): String = {
    val config = git.getRepository.getConfig
    config.load()
    val name = config.getString("remote", "origin", "url") match {
      case LineByLineDiffEmailHtml.GitHubUrl(repoName) => repoName
      case otherUrl => throw new Exception("Only github repos are supported currently... Can't use [%s] ".format(otherUrl))
    }

    name
  }


  // todo could be done better
  def only(repoDir: File, objectId: String): Iterable[RevCommit] = {
    val git = Git.open(repoDir)

    val commits = git.log
      .add(ObjectId.fromString(objectId))
      .setMaxCount(1)
      .call()

    logger.info("Got one commit from [%s]".format(repoDir))

    commits
  }
}
