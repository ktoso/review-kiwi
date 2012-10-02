package com.reviewkiwi.repoworker.git

import java.net.URI
import org.eclipse.jgit.api.Git
import java.io.File
import com.google.common.hash.Hashing
import com.weiglewilczek.slf4s.Logging
import collection.JavaConversions._

class GitCloner extends Logging {

  val ReposDir = new File("/tmp")

  def generateTargetDir(uri: URI): File = {
    val folder = Hashing.sha1().hashString(uri.toString).toString

    // todo be smarter ;-)

    val target = new File(ReposDir, folder)
    logger.info("Created target dir for [%s] in [%s] ".format(uri, target))
    //    cleanDir(target)
    target
  }

  def cleanDir(dir: File): Boolean = {
    import sys.process._
    ("rm -rf " + dir.getAbsolutePath).! == 0
  }

  // todo be smarter, if already exists, check if it's the same remote, then fetch, else just clone into ther dir
  def fetchOrClone(uri: URI) = {
    val targetDir = generateTargetDir(uri)
    val clonedAlready = Option(targetDir.list).map(_.contains(".git")).getOrElse(false)

    if (clonedAlready) {
      fetchChanges(uri, to = targetDir)
    } else {
      cloneRepo(uri, to = targetDir)
    }

    targetDir
  }

  /**
   * @return number of fetched changed
   */
  def fetchChanges(uri: URI, to: File, token: Option[String]) = {
    val git = Git.open(to)
    val fetchUri = token match {
      case None => uri.toString
      case Some(t) => uri.toString.replace("github.com", token + "@github.com")
    }

    val fetchResult = git.fetch
      .setRemote(fetchUri) // https://github.com/barthez/mysql.integra.dbfiller.git ->
      .setProgressMonitor(CliProgressMonitor)
      .call()

    Option(fetchResult.getTrackingRefUpdate("refs/remotes/origin/master")) match {
      case Some(status) =>
      case None => 0 // no updates
    }
  }

  /**
   * @return number of fetched changed
   */
  def cloneRepo(uri: URI, to: File): Int = {
    Git.cloneRepository
      .setURI(uri.toString)
      .setDirectory(to)
      .setProgressMonitor(CliProgressMonitor)
      .call()

    // count changes
    Git.open(to).log.all.call().size
  }
}
