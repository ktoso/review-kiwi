package com.reviewkiwi.repoworker.git

import java.net.URI
import org.eclipse.jgit.api.Git
import java.io.File
import com.google.common.hash.Hashing
import com.weiglewilczek.slf4s.Logging
import com.google.common.io.Files

class GitCloner extends Logging {

  val ReposDir = new File("/tmp")

  def generateTargetDir(uri: URI): File = {
    val folder = Hashing.sha1().hashString(uri.toString).toString

    // todo be smarter ;-)

    val target = new File(ReposDir, folder)
    logger.info("Created target dir for [%s] in [%s] ".format(uri, target))
    cleanDir(target)
    target
  }

  def cleanDir(dir: File): Boolean = {
    require(dir.isDirectory, "Should only be used on directories, which [%s] is not.".format(dir))

    import sys.process._
    ("rm -rf " + dir.getAbsolutePath).! == 0
  }

  // todo be smarter, if already exists, check if it's the same remote, then fetch, else just clone into ther dir
  def clone(uri: URI) = {
    val targetDir = generateTargetDir(uri)

    Git.cloneRepository
      .setURI(uri.toString)
      .setDirectory(targetDir)
      .setProgressMonitor(CliProgressMonitor)
      .call()
    targetDir
  }
}
