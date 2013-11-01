package com.reviewkiwi.repoworker

import akka.actor._
import Actor._
import data.{FetchNewChangesFromReposEach, FetchNewChangesFrom, FetchThisChange}
import fetch.{ChangeToFetchQueueFetcherActor, ChangesFetcherActor}
import git.{GitDiffer, FreshCommitsExtractor, GitCloner}
import notify.cli.CliNotifierActor
import java.net.URI
import notify.email.{EmailSenderActor, EmailNotifierActor}
import notify.template.html.LineByLineDiffEmailHtml
import com.reviewkiwi.common.email.EmailSender
import com.reviewkiwi.repoworker.fetcher.CheckQueue
import akka.util.duration._
import com.reviewkiwi.model.mongo.{Config, MongoInit}
import com.reviewkiwi.model._

object RegisterRunner extends App {

  val config = Config.readFromProperties()
  MongoInit.init(config)

  val user = KiwiUser.createRecord
    .name(readLine("name: "))
    .primaryEmail(readLine("email: "))
    .oauthToken(readLine("oauth token: "))
    .save(true)

  while(true) {
    println()
    val repo = KiwiRepository.createRecord
      .name(readLine("repo name: "))
      .githubRepoId(readLine("repoid: "))
      .fetchUrl(readLine("fetchUrl (https): "))
      .save(true)

    user.repos(repo.name.get :: user.repos.get).save(true)
  }


}
