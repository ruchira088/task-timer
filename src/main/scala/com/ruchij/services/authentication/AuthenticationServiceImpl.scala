package com.ruchij.services.authentication

import cats.data.OptionT
import cats.implicits._
import cats.{Applicative, ApplicativeError, MonadThrow, ~>}
import com.ruchij.dao.credentials.CredentialsDao
import com.ruchij.dao.user.models.{Email, User}
import com.ruchij.exceptions.AuthenticationException
import com.ruchij.kv.KeyspacedKeyValueStore
import com.ruchij.services.authentication.models.AuthenticationToken.AuthSecret
import com.ruchij.services.authentication.models.{AuthContext, AuthenticationToken}
import com.ruchij.services.hash.PasswordHashingService
import com.ruchij.services.user.UserService
import com.ruchij.services.user.models.Password
import com.ruchij.syntax.DateTimeOps
import com.ruchij.types.{JodaClock, RandomGenerator}

import java.util.UUID

class AuthenticationServiceImpl[F[_]: MonadThrow: JodaClock: RandomGenerator[*[_], UUID], A, G[_]](
  userService: UserService[F],
  passwordHashingService: PasswordHashingService[F],
  credentialsDao: CredentialsDao[G],
  keyspacedKeyValueStore: KeyspacedKeyValueStore[F, AuthSecret, AuthenticationToken]
)(implicit transaction: G ~> F) extends AuthenticationService[F] {

  override def login(email: Email, password: Password): F[AuthenticationToken] =
    for {
      user <- userService.getUserByEmail(email)
      credentials <-
        OptionT(transaction(credentialsDao.findByUserId(user.id)))
          .getOrElseF {
            ApplicativeError[F, Throwable].raiseError { AuthenticationException { "Credentials not found" } }
          }

      isSuccess <- passwordHashingService.checkPassword(credentials.hashedPassword, password)
      _ <-
        if (isSuccess) Applicative[F].pure((): Unit)
        else ApplicativeError[F, Throwable].raiseError { AuthenticationException("Invalid credentials") }

      timestamp <- JodaClock[F].timestamp
      secret <- RandomGenerator[F, UUID].generate.map(uuid => AuthSecret(uuid.toString))

      authToken =
        AuthenticationToken(user.id, timestamp, timestamp + AuthenticationService.SessionDuration, 0, secret)
    }
    yield authToken

  override def authenticate(secret: AuthSecret): F[AuthContext] =
    retrieveAuthToken(secret)
      .flatMap { token =>
        userService.getUserById(token.userId).map(user => AuthContext(user, token))
      }
      .flatTap {
        case AuthContext(_, token) =>
          JodaClock[F].timestamp.flatMap { timestamp =>
            val updatedToken =
              token.copy(renewals = token.renewals + 1, expiresAt = timestamp + AuthenticationService.SessionDuration)

            keyspacedKeyValueStore.put(secret, updatedToken)
          }
      }

  override def logout(secret: AuthSecret): F[User] =
    retrieveAuthToken(secret)
      .flatMap { authToken =>
        userService.getUserById(authToken.userId)
      }

  private def retrieveAuthToken(secret: AuthSecret): F[AuthenticationToken] =
    OptionT(keyspacedKeyValueStore.get(secret))
      .semiflatMap { authenticationToken =>
        JodaClock[F].timestamp
          .flatMap { timestamp =>
            if (authenticationToken.expiresAt.isAfter(timestamp)) Applicative[F].pure(authenticationToken)
            else
              ApplicativeError[F, Throwable].raiseError[AuthenticationToken] {
                AuthenticationException("Authentication token is expired")
              }
          }
      }
      .getOrElseF {
        ApplicativeError[F, Throwable].raiseError {
          AuthenticationException("Authentication token not found")
        }
      }
}
