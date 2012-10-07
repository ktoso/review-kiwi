package com.reviewkiwi.repoworker.data

import java.net.URI
import com.reviewkiwi.model.KiwiRepository
import akka.util.duration._
import akka.util.Duration

case class FetchThisChange(uri: URI, objectId: String)

case class FetchNewChangesFromReposEach(delay: Duration)
case class FetchNewChangesFrom(repo: KiwiRepository)
