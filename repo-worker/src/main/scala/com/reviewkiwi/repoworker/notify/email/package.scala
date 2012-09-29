package com.reviewkiwi.repoworker.notify

package object email {

  case class SendEmail(to: String, topic: String, body: String, replyTo: Option[String])
}
