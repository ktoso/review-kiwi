package com.reviewkiwi.repoworker.fetch

import akka.actor._
import akka.pattern.ask
import com.reviewkiwi.repoworker.data.{FetchNewChangesFromReposEach, FetchNewChangesFrom, FetchThisChange}
import com.reviewkiwi.repoworker.fetcher.NewCommit
import com.reviewkiwi.repoworker.git.{FreshCommitsExtractor, GitCloner}
import collection.JavaConversions._
import com.reviewkiwi.model.{KiwiRepository, KiwiUser}
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File
import java.net.URI
import com.weiglewilczek.slf4s.Logging
import akka.dispatch.{Await, ExecutionContext, Future}
import akka.util.duration._
import akka.util.{Duration, Timeout}

class ChangesFetcherActor(
    cloner: GitCloner,
    newChangesExtractor: FreshCommitsExtractor,
    onNewChangesActor: ActorRef,
    scheduler: Scheduler
  ) extends Actor with Logging {

  implicit val AtMost: Duration = 15.minutes
  implicit val Timeout: Timeout = AtMost

  implicit lazy val execContext = ExecutionContext.defaultExecutionContext(context.system)

  def receive = {
    case FetchThisChange(uri, objectId) =>

      val token = getToken(uri)

      val repoDir = cloner.fetchOrClone(uri, token)
      val changes = newChangesExtractor.only(repoDir, objectId)
      notifyAbout(changes, repoDir)

    case FetchNewChangesFrom(kiwiRepo) =>
      val uri = new URI(kiwiRepo.fetchUrl.is)
      val token = getToken(uri)

      logger.info("Fetch new changes from [%s]".format(uri))

      val repoDir = cloner.fetchOrClone(uri, token)
      val changes = newChangesExtractor.notYetNotifiedAbout(repoDir)
      notifyAbout(changes, repoDir)

      self ! FetchNewChangesFrom(kiwiRepo)

    case FetchNewChangesFromReposEach(delay) =>
      logger.info("Got fetch changes from each repo request, will execute and check again in [%s]".format(delay))

      KiwiRepository.findAllToFetch().map { repo =>
        logger.info("Sending FetchNewChangesFrom [%s] ".format(repo.githubRepoId))
        self ! FetchNewChangesFrom(repo)
      }
  }

  def getToken(uri: URI): Option[String] = {
    // todo replace with user per repo etc token
    import com.foursquare.rogue.Rogue._
    val ktoso = KiwiUser.get().get // todo hardcoded
    Some(ktoso.oauthToken.is)
  }

  def notifyAbout(changes: Iterable[RevCommit], repoDir: File) {
    changes foreach { commit =>
      logger.info("Will have to notify about [%s] in [%s] ".format(commit.getName, repoDir))

      onNewChangesActor ! NewCommit(commit, in = repoDir)
    }
  }
}
