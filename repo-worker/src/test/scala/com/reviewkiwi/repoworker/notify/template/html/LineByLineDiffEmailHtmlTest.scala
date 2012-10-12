package com.reviewkiwi.repoworker.notify.template.html

import org.scalatest.{FlatSpec, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.eclipse.jgit.api.Git
import java.io.File
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk
import org.eclipse.jgit.lib.ObjectId
import com.google.common.io.Files
import com.google.common.base.Charsets

class LineByLineDiffEmailHtmlTest extends FlatSpec with ShouldMatchers {
  it should "extract the repository name from a github url" in {
    // given
    val url = "https://github.com/ktoso/yap-chef.git"

    // when
    val LineByLineDiffEmailHtml.GitHubUrl(repoName) = url

    // then
    repoName should equal ("ktoso/yap-chef")
  }

  it should "prepare the template" in {
    // given
    val html = new LineByLineDiffEmailHtml()
    val git = Git.open(new File("/tmp/988c76edd0311293a57155075e476736d644d36a"))
    val walk = new RevWalk(git.getRepository, 30)
    val commit = walk.parseCommit(ObjectId.fromString("e662223d3d0104e1cc23727f00713e7e693bc3b2"))
    walk.dispose()

    // when
    val build = html.build(git, commit, Nil)

    // then
    Files.write(build, new File("/tmp/kiwi.html"), Charsets.UTF_8)
  }
}
