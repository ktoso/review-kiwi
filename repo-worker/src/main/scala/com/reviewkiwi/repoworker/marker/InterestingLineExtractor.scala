package com.reviewkiwi.repoworker.marker

trait InterestingLineExtractor {

  def extract(fileName: String, line: String, number: Int): Option[InterestingLine]
}