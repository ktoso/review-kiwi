package com.reviewkiwi.repoworker

import org.eclipse.jgit.revwalk.RevCommit
import java.io.File

package object fetcher {

  case object CheckQueue

  case class NewCommit(revCommit: RevCommit, in: File)
}
