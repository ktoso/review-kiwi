package code.snippet.upload

import net.liftweb.util._
import Helpers._
import net.liftweb.common.Box
import net.liftweb.http.js.JsCmds.Script
import net.liftweb.http.LiftRules
import net.liftweb.http.js.JsCmd

trait AjaxImageUpload {

  def targetUrl: String
  def params: Map[String, Box[_]]

  lazy val paramsString = {
    val q = params.filter(e => e._2.isDefined).map(e => e._1 + "=" + e._2.get).mkString("&")
    targetUrl + "?" + q
  }

  def prepareUploaderJsCmd = new JsCmd {
    def toJsCmd =
      """
      new qq.FileUploader({
        element: document.getElementById('file-uploader'),
        action: '""" + paramsString + """',
        debug: true,
        allowedExtensions: ['jpg', 'jpeg', 'png', 'gif']
      });"""
  }

  def form: CssSel = {
    ".upload-form" #> <div>
      <div id="file-uploader">
        <noscript>
          <p>Please enable JavaScript to use file uploader.</p>
        </noscript>
      </div>
      {Script(LiftRules.jsArtifacts.onLoad(prepareUploaderJsCmd))}
    </div>

  }
}
