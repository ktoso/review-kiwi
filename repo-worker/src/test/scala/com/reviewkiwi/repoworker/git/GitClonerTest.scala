package com.reviewkiwi.repoworker.git

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.net.URI

class GitClonerTest extends FlatSpec with ShouldMatchers {

  val cloner = new GitCloner

  "authorizeGithubUrlWithOAuth" should "should add auth part to url" in {
    // given
    val token = Some("abcabcabcabc")
    val uri = new URI("https://github.com/ktoso/review-kiwi.git")

    // when
    val authorized = cloner.authorizeGithubUrlWithOAuth(token, uri)

    // then
    authorized should equal ("https://abcabcabcabc@github.com/ktoso/review-kiwi.git")
  }
}
