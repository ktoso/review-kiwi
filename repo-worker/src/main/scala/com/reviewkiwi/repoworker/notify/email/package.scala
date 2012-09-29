package com.reviewkiwi.repoworker.notify

package object email {

  import net.liftweb.util.Mailer._
  case class SendEmail(to: To, topic: String, body: String)
}
