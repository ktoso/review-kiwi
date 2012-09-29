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

  object config {
    val mailerUsername = "test-mailer@project13.pl"
    val mailerPassword = "bananbananban"
    val mailerHost = "smtp.gmail.com"
  }

  lazy val initMailer = {
    Mailer.authenticator = Full(new Authenticator() {
      override def getPasswordAuthentication = new PasswordAuthentication(config.mailerUsername, config.mailerPassword)
    })

    Mailer.customProperties = config.mailerHost match {

      case "smtp.gmail.com" => Map(
        "mail.smtp.host" -> "smtp.gmail.com",
        "mail.smtp.port" -> 587.toString,
        "mail.smtp.auth" -> true.toString,
        "mail.smtp.starttls.enable" -> true.toString
      )

      case host => Map(
        "mail.smtp.host" -> host,
        "mail.smtp.port" -> 25.toString,
        "mail.smtp.auth" -> false.toString
      )
    }
  }


  def receive = {
    case SendEmail(to, title, body, replyTo) =>
      initMailer

      emailSender.send(to, title, body, replyTo = replyTo)
  }
}
