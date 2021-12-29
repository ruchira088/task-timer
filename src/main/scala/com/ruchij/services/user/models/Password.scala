package com.ruchij.services.user.models

case class Password(value: String) extends AnyVal

object Password {
  def from(passwordString: String): Either[String, Password] = ???
}
