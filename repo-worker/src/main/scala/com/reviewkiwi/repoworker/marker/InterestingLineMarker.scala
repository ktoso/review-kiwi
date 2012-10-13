package com.reviewkiwi.repoworker.marker

trait InterestingLineMarker {

  def extract(fileName: String, line: String, number: Int): Option[InterestingLine]
}