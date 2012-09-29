package com.reviewkiwi.repoworker

import data.RepoLocation
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File

package object fetcher {

  // todo remove me
  case class Changeset() // todo that's a mock

  case class FetchChangesFrom(repo: RepoLocation)
  case class NewCommit(revCommit: RevCommit, in: File)
}
