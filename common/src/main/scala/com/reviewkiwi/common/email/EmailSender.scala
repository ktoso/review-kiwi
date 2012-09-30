package com.reviewkiwi.common.email

import org.apache.commons.mail.HtmlEmail
import collection.JavaConversions._
import javax.mail.internet.InternetAddress
import scalaz.Scalaz._

class EmailSender {

  val FromEmail = "review-kiwi@project13.pl"
  val FromName = "Review Kiwi"

  // todo that's a mock
  object config {
    val mailerUsername = "test-mailer@project13.pl"
    val mailerPassword = "bananbananban"
    val mailerHost = "smtp.gmail.com"
    val mailerPort = 587
    val mailerTLS = true
  }

  def send(to: String, title: String, htmlBody: String, txtBody: Option[String] = None, replyTo: Option[String] = None) {
    val email = new HtmlEmail

    email.setHostName(config.mailerHost)
    email.setAuthentication(config.mailerUsername, config.mailerPassword)
    email.setSmtpPort(config.mailerPort)
    email.setTLS(config.mailerTLS)
    email.setCharset("UTF-8")

    email.setSubject(title)

    email.setFrom(FromEmail, FromName)
    replyTo map { addr => email.setReplyTo(emailAddress(addr)) }

    email.setHtmlMsg(htmlBody)
    txtBody map { email.setTextMsg(_) }

    email.setTo(emailAddress(to))

    email.send()
  }

  private[email] def emailAddress(mail: String) =
    List(new InternetAddress(mail))

}