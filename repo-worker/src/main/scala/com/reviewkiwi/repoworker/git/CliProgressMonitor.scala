package com.reviewkiwi.repoworker.git

import org.eclipse.jgit.lib.ProgressMonitor
import com.reviewkiwi.common.cli.ProgressPrinter
import scalaz.Scalaz._

class CliProgressMonitor extends ProgressMonitor {

  var totalTasks = 0
  var currentTask = 0
  var maybeTitle: Option[String] = None
  var progress: Option[ProgressPrinter] = _

  def start(totalTasks: Int) {
    this.totalTasks = totalTasks
  }

  def beginTask(title: String, totalWork: Int) {
    currentTask += 1
    println("[%d/%d] %s: %d...".format(currentTask, totalTasks, title, totalWork))
    progress = new ProgressPrinter(totalWork).some
  }

  def update(completed: Int) {
    progress map { _.inc() }
  }

  def endTask() {}

  def isCancelled = false
}

object CliProgressMonitor extends CliProgressMonitor
