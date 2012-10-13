package com.reviewkiwi.repoworker.marker

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class TodoLineMarkerTest extends FlatSpec with ShouldMatchers {

  it should "extract a todo line" in {
    // given
    val line = " // todo fix bug"

    // when
    val interestingLine = (TodoLineMarker).extract("test.txt", line, 23)

    // then
    interestingLine should be ('defined)
  }
}
