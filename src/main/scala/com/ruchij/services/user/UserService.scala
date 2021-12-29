package com.ruchij.services.user

import com.ruchij.dao.user.models.{Email, User}
import com.ruchij.services.user.models.Password

trait UserService[F[_]] {
  def create(firstName: String, lastName: Option[String], email: Email, password: Password): F[User]
}
