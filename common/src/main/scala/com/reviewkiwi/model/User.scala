package com.reviewkiwi.model

case class User(
                 email: String,
                 name: String,
                 surname: String,
                 secondaryEmails: List[String] = Nil)

object User {
  def findAll = User("ktoso@project13.pl", "Konrad", "Malawski", List("konrad.malawski@java.pl"))
}
