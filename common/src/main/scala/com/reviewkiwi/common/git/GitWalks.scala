package com.reviewkiwi.common.git

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk

trait GitWalks {

  // todo should have limits
  def withWalk[T](depth: Int = 3)(block: RevWalk => T)(implicit repo: Repository) = {
    val walk = new RevWalk(repo, depth)

    try {
      block(walk)
    } finally {
      walk.dispose()
    }
  }
}
