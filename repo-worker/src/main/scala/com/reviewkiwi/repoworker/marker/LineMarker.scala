package com.reviewkiwi.repoworker.marker

trait LineMarker {
  def lineIsInteresting(inputLine: String): Boolean

  def mark(inputLine: String, above: String): Option[String]
}
