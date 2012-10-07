package com.reviewkiwi.repoworker.git

import org.eclipse.jgit.lib.ProgressMonitor

class NoopProgressMonitor extends ProgressMonitor {

  def start(totalTasks: Int) {
  }

  def beginTask(title: String, totalWork: Int) {
  }

  def update(completed: Int) {
  }

  def endTask() {}

  def isCancelled = false
}

object NoopProgressMonitor extends NoopProgressMonitor
