package com.ruchij.services.hash

import com.ruchij.services.user.models.Password

trait PasswordHashingService[F[_]] extends HashingService[F] {
  def checkPassword(hashedPassword: String, password: Password): F[Boolean]
}
