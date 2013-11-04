package com.reviewkiwi.repoworker.notify.email

import akka.actor._
import com.reviewkiwi.common.email.EmailSender

class EmailSenderActor(emailSender: EmailSender) extends Actor {

  def receive = {
    case SendEmail(to, title, body, replyTo) =>
      emailSender.send(to, title, body, replyTo = replyTo)
  }
}