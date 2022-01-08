package com.ruchij.services.authentication.models

import com.ruchij.services.authentication.models.AuthenticationToken.AuthSecret
import org.joda.time.DateTime

case class AuthenticationToken(
  userId: String,
  issuedAt: DateTime,
  expiresAt: DateTime,
  renewals: Long,
  secret: AuthSecret
)

object AuthenticationToken {
  case class AuthSecret(value: String) extends AnyVal
}
