package code.lib.js

import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import net.liftweb.common.Logger

object YapJsCmd extends Logger {

  case class AppendText(id: String, line: String) extends JsCmd {
    def toJsCmd = {
      val uid = ("#" + id).encJs
      val escapedMsg = line.encJs

      """ try {
            jQuery(%s).append(%s).append("\n");
          } catch (e) { console.log(e) }"""
        .format(uid, escapedMsg)
    }
  }

  case class Alert(txt: String) extends JsCmd {
    def toJsCmd = """alert("%s")""".format(txt)
  }
}
