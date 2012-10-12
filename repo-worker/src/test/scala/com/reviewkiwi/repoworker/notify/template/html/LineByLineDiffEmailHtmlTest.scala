package com.reviewkiwi.repoworker.notify.template.html

import org.scalatest.{FlatSpec, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.eclipse.jgit.api.Git
import java.io.File
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk
import org.eclipse.jgit.lib.ObjectId
import com.google.common.io.Files
import com.google.common.base.Charsets
import com.reviewkiwi.repoworker.git.{GitDiffer, FreshCommitsExtractor}

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
    val repoDir = new File("/Users/ktoso/code/softwaremill/review-kiwi")
    val git = Git.open(repoDir)
    val walk = new RevWalk(git.getRepository, 1)
    val commit = walk.parseCommit(ObjectId.fromString("e662223d3d0104e1cc23727f00713e7e693bc3b2"))
    walk.dispose()

    val extractor = new FreshCommitsExtractor
    val head = extractor.head(new File(repoDir, ".git"))

    val differ = new GitDiffer
    val diff = differ.diffWithParent(git, head)

    // when
    val build = html.build(git, commit, diff)

    // then
    Files.write(build, new File("/tmp/kiwi.html"), Charsets.UTF_8)
  }
}
