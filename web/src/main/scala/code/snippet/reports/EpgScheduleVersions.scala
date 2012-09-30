package code.snippet.reports

import net.liftweb.http.SHtml

import net.liftweb.util._
import Helpers._
import net.liftweb.http.js.JsCmds
import xml.Text

class EpgScheduleVersions {
  def buildResetButton =
    "#reset_do [onclick]" #> SHtml.ajaxInvoke(() => {
      val timestamp = tv.yap.model.epg.EpgScheduleVersion.reset_all
      JsCmds.SetHtml("reset_status",Text("OK, updated the timestamp to '"+timestamp + "',")) & JsCmds.SetHtml("reset_do",Text("Reset again"))
    })
}
