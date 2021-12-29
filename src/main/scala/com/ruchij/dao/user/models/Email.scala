package com.ruchij.dao.user.models

case class Email(address: String) extends AnyVal

object Email {
  def from(emailAddress: String): Either[String, Email] = ???
}
