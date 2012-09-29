package com.reviewkiwi.repoworker.notify.template.html

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.api.Git

trait HtmlReport {

  def build(git: Git, diffs: Iterable[DiffEntry]): String
}
