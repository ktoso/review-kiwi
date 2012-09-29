package com.reviewkiwi.repoworker.notify.email

import akka.actor._
import net.liftweb.util.Mailer
import net.liftweb.common.Full
import javax.mail.{PasswordAuthentication, Authenticator}
import xml.XML
import net.liftweb.util.Mailer.From

class EmailSenderActor extends Actor {

  val printEmail = true // todo debug stuff
  val reallySendEmails = true // todo debug stuff

  val FromKiwi = From("review-kiwi@project13.pl", Some("Review Kiwi"))

  // todo that's a mock
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
        "mail.smtp.starttls.enable" -> true.toString)

      case host => Map(
        "mail.smtp.host" -> host,
        "mail.smtp.port" -> 25.toString,
        "mail.smtp.auth" -> false.toString
      )
    }
  }


  def receive = {
    case SendEmail(to, topic, body) =>
      import Mailer._
      initMailer

      if (printEmail)
        println("body = " + body)

      if(reallySendEmails)
        blockingSendMail(
          FromKiwi,
          Subject(topic),
          XHTMLMailBodyType(XML.loadString(body)) :: List(to): _*
        )
  }
}
