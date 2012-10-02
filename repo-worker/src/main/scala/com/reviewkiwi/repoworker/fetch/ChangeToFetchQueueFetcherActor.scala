package com.reviewkiwi.repoworker.fetch

import akka.actor._
import akka.actor.{ActorRef, Actor}
import com.reviewkiwi.model.{ChangeFetched, KiwiRepository, ChangeToFetch}
import com.reviewkiwi.repoworker.data.FetchThisChange
import java.net.URI
import com.weiglewilczek.slf4s.Logging
import com.reviewkiwi.repoworker.fetcher.CheckQueue

class ChangeToFetchQueueFetcherActor(changesFetcher: ActorRef) extends Actor with Logging {

  def receive = {
    case CheckQueue =>
      logger.info("Checking for new changes to fetch...")

      ChangeToFetch.findAll foreach { fetchMe =>
        logger.info("Will try to fetch changes for [%s][%s] ".format(fetchMe.repo.is, fetchMe.id.is))

        if(alreadyFetched(fetchMe)) fetchMe.delete_!
        else changesFetcher ! FetchThisChange(new URI(fetchMe.repo.obj.map(_.fetchUrl.is).get), fetchMe.objectId.is)
      }
  }

  def alreadyFetched(change: ChangeToFetch): Boolean = {
    import com.foursquare.rogue.Rogue._
    (ChangeFetched where(_.objectId eqs change.objectId.is) count()) > 0 // todo improve me, should take repo name too
  }
}
