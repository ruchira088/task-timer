package com.ruchij.services.authentication

import com.ruchij.dao.user.models.{Email, User}
import com.ruchij.services.authentication.models.{AuthContext, AuthenticationToken}
import com.ruchij.services.authentication.models.AuthenticationToken.AuthSecret
import com.ruchij.services.user.models.Password

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

trait AuthenticationService[F[_]] {
  def login(email: Email, password: Password): F[AuthenticationToken]

  def authenticate(secret: AuthSecret): F[AuthContext]

  def logout(secret: AuthSecret): F[User]
}

object AuthenticationService {
  val SessionDuration: FiniteDuration = 1 hour
}
