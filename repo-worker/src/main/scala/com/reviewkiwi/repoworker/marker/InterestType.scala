package com.reviewkiwi.repoworker.marker

object InterestType extends Enumeration {
  type InterestType = Value

  val
    FixMe,
    ToDo,
    Warning,
    AntiPattern,
    ProbableError,
    StyleIssue = Value
}