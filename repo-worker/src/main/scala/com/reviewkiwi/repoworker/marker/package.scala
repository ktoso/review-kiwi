package com.reviewkiwi.repoworker

package object marker {

  case class InterestingLine(
    fileName: String,
    line: String,
    lineNumber: Int,
    interestType: InterestType.Value,
    suggestion: Option[String] = None
  )
}
