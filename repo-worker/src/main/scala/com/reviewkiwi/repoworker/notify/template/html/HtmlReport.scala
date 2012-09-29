package com.reviewkiwi.repoworker.notify.template.html

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit

trait HtmlReport {

  def build(git: Git, commit: RevCommit, diffs: Iterable[DiffEntry]): String

}
