package com.reviewkiwi.common.git

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers

class GitDiffsTest extends FlatSpec with ShouldMatchers
  with MockitoSugar
  with GitDiffs {

  it should "present numbered lines, based on @@ lines" in {
    // given
    val presenter = new DiffEntryPresenter(null)(null) {
      override def asDiffString = {
        """
          |diff --git a/repo-worker/src/main/resources/template/email/email.mustache b/repo-worker/src/main/resources/template/email/email.mustache
          |index 22ad548..d8ef557 100644
          |--- a/repo-worker/src/main/resources/template/email/email.mustache
          |+++ b/repo-worker/src/main/resources/template/email/email.mustache
          |@@ -145,7 +145,7 @@
          |     <div class="files-listing">
          |         <h2>Modified Files: </h2>
          |         {{#modifiedFiles}}
          |-            {{actionIcon}} {{action}}: {{path}}<b>{{fileName}}</b> <br/>
          |+            {{actionIcon}} {{action}}: {{{displayFileName}}} <br/>
          |         {{/modifiedFiles}}
          |     </div>
          | </td>
          |@@ -180,16 +180,18 @@
          |     <h2>Interesting Lines: </h2>
          |     <div>
          |         {{#interestingLines}}
          |+        <div style="border: solid 1px gray; padding: 0.5em; margin-bottom: 0.5em;">
          |             [{{interestType}}] in <b>{{fileName}}</b> @ {{lineNumber}} <br/>
          |+
          |+            <pre style="background: #ececec"><code>{{line}}</code></pre>
          |
          |             {{#suggestion}}
          |             <blockquote style="margin: 1em 3em;padding: .5em 1em;border-left: 5px solid #fce27c;background-color: #f6ebc1; ">
          |-                <h5>Suggestion:</h5>
          |-                <p>{{suggestion}}</p>
          |+                <b>Suggestion:</b> <br>
          |+                {{{suggestion}}}
          |             </blockquote>
          |             {{/suggestion}}
          |-
          |-            <pre><code>{{line}}</code></pre>
          |+        </div>
          |         {{/interestingLines}}
          |     </div>
          | </div>
        """.stripMargin
      }
    }

    // when
    val lines = presenter.asNumberedDiffLines

    // then
    lines should contain (NumberedDiffEntryLine("""@@ -145,7 +145,7 @@""", 0))
    lines should contain (NumberedDiffEntryLine("""     <div class="files-listing">""", 145))
    lines should contain (NumberedDiffEntryLine(""" </td>""", 152))

    lines should contain (NumberedDiffEntryLine("""@@ -180,16 +180,18 @@""", 0))
    lines should contain (NumberedDiffEntryLine("""+        <div style="border: solid 1px gray; padding: 0.5em; margin-bottom: 0.5em;">""", 183))
  }
}
