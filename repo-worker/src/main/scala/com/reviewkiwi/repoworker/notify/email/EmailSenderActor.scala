package com.reviewkiwi.repoworker.notify.email

import akka.actor._
import net.liftweb.util.Mailer
import net.liftweb.common.Full
import javax.mail.{PasswordAuthentication, Authenticator}
import xml.XML
import net.liftweb.util.Mailer.From
import com.reviewkiwi.common.email.EmailSender
import scalaz.Scalaz._

class EmailSenderActor(emailSender: EmailSender) extends Actor {

  def receive = {
    case SendEmail(to, title, body, replyTo) =>
      emailSender.send(to, title, body, replyTo = replyTo)
  }
}
