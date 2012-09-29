package com.reviewkiwi.repoworker.git

import java.io.File
import org.eclipse.jgit.api.Git
import java.lang.Iterable
import org.eclipse.jgit.revwalk.RevCommit
import com.weiglewilczek.slf4s.Logging
import org.joda.time.DateTime

class FreshCommitsExtractor extends Logging {

  // todo obviously fix ;-)
  def lastThree(repoDir: File): Iterable[RevCommit] = {
    val git = Git.open(repoDir)

    val commits = git.log
      .setMaxCount(1)
      .call()

    logger.info("Got commits from [%s]".format(repoDir))

    commits
  }
}
