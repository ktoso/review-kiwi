package com.reviewkiwi.repoworker.git

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.net.URI

class GitClonerTest extends FlatSpec with ShouldMatchers {

  val cloner = new GitCloner

  it should "clone by fetching" in {
    // given
    val token = Some("e0915567a4a4b02d2a1b731997050bc3642a95d5")
    val uri = new URI("https://github.com/ktoso/review-kiwi.git")

    // when
    cloner.fetchOrClone(uri, token)
    cloner.fetchOrClone(uri, token)
  }
}
