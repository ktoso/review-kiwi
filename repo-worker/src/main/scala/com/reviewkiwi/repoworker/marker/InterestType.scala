package com.reviewkiwi.repoworker.marker

object InterestType extends Enumeration {
  type InterestType = Value

  val
    FixMe,
    ToDo,
    Warning,
    Caution,
    AntiPattern,
    ProbableError,
    StyleIssue = Value
}