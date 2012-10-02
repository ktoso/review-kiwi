package com.reviewkiwi.repoworker.fetch

import akka.actor._
import com.reviewkiwi.repoworker.data.FetchThisChange
import com.reviewkiwi.repoworker.fetcher.NewCommit
import com.reviewkiwi.repoworker.git.{FreshCommitsExtractor, GitCloner}
import collection.JavaConversions._

class ChangesFetcherActor(
    cloner: GitCloner,
    newChangesExtractor: FreshCommitsExtractor,
    onNewChangesActor: ActorRef
  ) extends Actor {

  def receive = {
    case FetchThisChange(uri, objectId) =>
      val repoDir = cloner.fetchOrClone(uri)
      val changes = newChangesExtractor.only(repoDir, objectId)
      changes foreach { commit => onNewChangesActor ! NewCommit(commit, in = repoDir) }
  }
}
