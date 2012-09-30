package com.reviewkiwi.common.git

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers

class GitDiffsTest extends FlatSpec with ShouldMatchers
  with MockitoSugar
  with GitDiffs {

  "GitDiffPresenter" should "present a change as preformatted html" in {
    // given
    val presenter = new DiffEntryPresenter(null)(null) {
      override def asDiffString = {
        """
          |diff --git a/README.md b/README.md
          |index 3935687..04f5f90 100644
          |--- a/README.md
          |+++ b/README.md
          |@@ -46,6 +46,12 @@
          | You can us it as a mixin `with Rainbow` or `import Rainbow._` or import the **package object** `import pl.project13.scala.rainbow._`.
          | For a list of available colors take a look at <a href="https://github.com/ktoso/scala-rainbow/blob/master/src/main/scala/pl/project13/scala/rainbow/Rainbow.scala">Rainbow.scala</a>.
          |
          |+Yay, feedback!
          |+-------------
          |+
          |+> btw I really like your lib!<br/>
          |+> It reminds me of "node-colors" for node.js
          |+
          | License
          | -------
          | Whatever ;-) Just use it however you see fit.
          |
        """.stripMargin
      }
    }

    // when
    val html = presenter.asDiffHTML

    // then
    html should include("""<pre class="insert">Yay, feedback!</pre>""")
    html should not include("""diff --git""")
    html should not include("""--- a/README.md""")
  }
}
