package com.reviewkiwi.repoworker.notify.cli

import akka.actor.Actor
import akka.actor._
import com.reviewkiwi.repoworker.fetcher.NewCommit
import org.eclipse.jgit.api.Git
import com.reviewkiwi.repoworker.git.GitDiffer
import org.eclipse.jgit.diff.{DiffFormatter, DiffEntry}
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File

class CliNotifierActor(differ: GitDiffer) extends Actor {

  case class Diffs(diffs: List[DiffEntry])

  def receive = {
    case NewCommit(revCommit, repoDir) =>
      onNewCommit(Git.open(repoDir), revCommit)

    case Diffs(diffs) =>
      diffs foreach { diff => println(diff) }

    case _ =>
  }

  def onNewCommit(git: Git, revCommit: RevCommit) {
    println("[%s] by [%s]: %s".format(revCommit.getName, revCommit.getAuthorIdent.getName, revCommit.getShortMessage))
    self ! Diffs(differ.diffWithParent(git, revCommit))
  }
}
