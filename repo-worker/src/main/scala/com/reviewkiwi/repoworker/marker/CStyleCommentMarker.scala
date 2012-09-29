package com.reviewkiwi.repoworker.marker

abstract class CStyleCommentMarker extends LineMarker {
  override def lineIsInteresting(line: String) = line contains "//"
}
