package com.reviewkiwi.repoworker.git

import java.net.URI
import org.eclipse.jgit.api.Git
import java.io.File
import com.google.common.hash.Hashing
import com.weiglewilczek.slf4s.Logging
import collection.JavaConversions._
import com.reviewkiwi.model.KiwiUser
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import javax.smartcardio.Card
import org.eclipse.jgit.lib.{RepositoryBuilder, Repository}

class GitCloner extends Logging {

  val ReposDir = new File("/tmp")

  def generateTargetDir(uri: URI): File = {
    val folder = Hashing.sha1().hashString(uri.toString).toString

    val target = new File(ReposDir, folder)
    logger.info("Created target dir for [%s] in [%s] ".format(uri, target))
    target
  }

  def cleanDir(dir: File): Boolean = {
    logger.info("Cleaning contents of [%s]".format(dir.getAbsolutePath))
    import sys.process._
    ("rm -rf " + dir.getAbsolutePath).! == 0
  }

  /**
   * @return directory where the cloned repo is
   */
  def fetchOrClone(uri: URI, token: Option[String]): File = {
    val targetDir = generateTargetDir(uri)

    val clonedAlready = Option(targetDir.list).map(_.contains(".git")).getOrElse(false)

    if (clonedAlready) {
      fetchChanges(uri, to = targetDir, token = token)
    } else {
      initRepoAndFetch(uri, to = targetDir, token = token)
    }

    targetDir
  }

  /**
   * @return number of fetched changed
   */
  def fetchChanges(uri: URI, to: File, token: Option[String]): Int = {
    logger.info("Fetching changes from [%s] to [%s] ".format(uri, to))

    val repo = (new RepositoryBuilder).setGitDir(new File(to, ".git")).build()
    val git = Git.wrap(repo)

    val origin = "origin"

    git.getRepository.getConfig.load()
    git.getRepository.getConfig.setString("remote", origin, "url", uri.toString)
    git.getRepository.getConfig.setString("remote", origin, "fetch", "+refs/heads/*:refs/remotes/origin/*")
    git.getRepository.getConfig.save()

    val command = git.fetch
      .setRemote(origin)
      .setProgressMonitor(NoopProgressMonitor)

    command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token.get, "x-oauth-basic"))

    val fetchResult = command.call()

    Option(fetchResult.getTrackingRefUpdate("refs/remotes/origin/master")) match {
      case Some(status) => 1
      case None => 0 // no updates
    }
  }

  /**
   * @return number of fetched changed
   */
  def initRepoAndFetch(uri: URI, to: File, token: Option[String]): Int = {
    logger.info("Initializing repo in [%s] ".format(to))

    Git.init
      .setDirectory(to)
      .call()

    fetchChanges(uri, to, token)
  }

}
