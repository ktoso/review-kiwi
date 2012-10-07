package com.reviewkiwi.repoworker.notify.template.html

import org.scalatest.{FlatSpec, FunSuite}
import org.scalatest.matchers.ShouldMatchers

class LineByLineDiffEmailHtmlTest extends FlatSpec with ShouldMatchers {
  it should "extract the repository name from a github url" in {
    // given
    val url = "https://github.com/ktoso/yap-chef.git"

    // when
    val LineByLineDiffEmailHtml.GitHubUrl(repoName) = url

    // then
    repoName should equal ("ktoso/yap-chef")
  }
}
