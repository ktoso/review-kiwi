package com.reviewkiwi.repoworker.git

import java.io.File
import org.eclipse.jgit.api.Git
import java.lang.Iterable
import org.eclipse.jgit.revwalk.RevCommit
import com.weiglewilczek.slf4s.Logging
import org.joda.time.DateTime
import org.eclipse.jgit.lib.ObjectId
import collection.JavaConversions._
import com.reviewkiwi.model.ChangeFetched

class FreshCommitsExtractor extends Logging {

  val MaxFreshCommits = 20

  def alreadyNotifiedAbout(commit: RevCommit) = {
    import com.foursquare.rogue.Rogue._
    ChangeFetched where(_.objectId eqs commit.getName) exists()
  }

  // todo obviously fix ;-)
  def notYetNotifiedAbout(repoDir: File): Iterable[RevCommit] = {
    val git = Git.open(repoDir)

    val commits = git.log
      .setMaxCount(MaxFreshCommits)
      .call()

    commits.filterNot(alreadyNotifiedAbout)


    logger.info("Got commits from [%s]".format(repoDir))

    commits
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
