package com.reviewkiwi.repoworker

import akka.actor._
import Actor._
import data.GitRepoLocation
import fetch.ChangesFetcherActor
import git.{GitDiffer, FreshCommitsExtractor, GitCloner}
import notify.cli.CliNotifierActor
import java.net.URI
import notify.email.{EmailSenderActor, EmailNotifierActor}
import notify.template.html.LineByLineDiffEmailHtml
import com.reviewkiwi.common.email.EmailSender

object RepoWorkerRunner extends App {

  val system = ActorSystem("RepoWorkerSystem")

  // impls
  val cloner = new GitCloner
  val differ = new GitDiffer
  val emailSender = new EmailSender
  val extractor = new FreshCommitsExtractor
  val lineByLineDiffHtmlReporter = new LineByLineDiffEmailHtml


  val cliNotifierActor = system.actorOf(
    Props(new CliNotifierActor(differ)),
    name = "cli-notifier"
  )

  val emailSenderActor = system.actorOf(
    Props(new EmailSenderActor(emailSender)),
  name = "email-sender"
  )
  val emailNotifierActor = system.actorOf(
    Props(new EmailNotifierActor(differ, lineByLineDiffHtmlReporter, emailSenderActor)),
    name = "email-notifier"
  )

  val fetcherActor = system.actorOf(
//    Props(new ChangesFetcherActor(cloner, extractor, cliNotifierActor)),
    Props(new ChangesFetcherActor(cloner, extractor, emailNotifierActor)),
    name = "fetcher"
  )


  // mocking interaction
  fetcherActor ! GitRepoLocation(new URI("https://github.com/ktoso/scala-rainbow.git"))
}
