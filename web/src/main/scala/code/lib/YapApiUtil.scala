package code.lib

import bootstrap.liftweb.LiftedConfig
import tv.yap.common.io.URLUtil
import net.liftweb.http.S
import java.io.IOException

object YapApiUtil {
  def callYapApi(relativeUrl: String, method: String = "GET", successMessage: => String, failureMessage: => String): Boolean = {
    try {
      val response = URLUtil.fetchStatusCode(LiftedConfig.is.apiEndpoint + relativeUrl, method)

      if (response < 300) {
        S.notice(successMessage)
        true
      } else {
        S.error(failureMessage + response)
        false
      }
    } catch {
      case e: IOException => {
        S.error(failureMessage + e.getMessage)
        false
      }
    }
  }
}
