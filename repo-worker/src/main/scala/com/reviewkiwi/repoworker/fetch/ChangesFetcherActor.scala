package com.reviewkiwi.repoworker.fetch

import akka.actor._
import com.reviewkiwi.repoworker.data.GitRepoLocation
import com.reviewkiwi.repoworker.fetcher.{Changeset, NewCommit}
import org.eclipse.jgit.api.Git
import com.google.common.hash.Hashing
import com.reviewkiwi.repoworker.git.{FreshCommitsExtractor, GitCloner}
import collection.JavaConversions._

class ChangesFetcherActor(
    cloner: GitCloner,
    newChangesExtractor: FreshCommitsExtractor,
    onNewChangesActor: ActorRef
  ) extends Actor {

  def receive = {
    case GitRepoLocation(uri) =>
      val repoDir = cloner.clone(uri)
      val changes = newChangesExtractor.lastThree(repoDir)
      changes foreach { commit => onNewChangesActor ! NewCommit(commit, in = repoDir) }
  }
}
