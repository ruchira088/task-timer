package com.ruchij.services.user

import cats.data.OptionT
import cats.effect.kernel.Sync
import cats.implicits._
import cats.{MonadThrow, ~>}
import com.ruchij.dao.credentials.CredentialsDao
import com.ruchij.dao.credentials.model.Credentials
import com.ruchij.dao.user.UserDao
import com.ruchij.dao.user.models.{Email, User}
import com.ruchij.exceptions.ResourceConflictException
import com.ruchij.services.hash.HashingService
import com.ruchij.services.user.models.Password
import com.ruchij.types.{JodaClock, Logger, RandomGenerator}

import java.util.UUID

class UserServiceImpl[F[_]: Sync: JodaClock: RandomGenerator[*[_], UUID], G[_]: MonadThrow](
  hashingService: HashingService[F],
  userDao: UserDao[G],
  credentialsDao: CredentialsDao[G]
)(implicit transaction: G ~> F)
    extends UserService[F] {

  override def create(firstName: String, lastName: Option[String], email: Email, password: Password): F[User] =
    for {
      timestamp <- JodaClock[F].timestamp
      userId <- RandomGenerator[F, UUID].generate.map(_.toString)
      hashedPassword <- hashingService.hash(password.value)

      user = User(userId, timestamp, firstName, lastName, email)
      credentials = Credentials(userId, timestamp, hashedPassword)

      _ <-
        transaction {
          OptionT(userDao.findByEmail(email)).isEmpty
            .flatMap { isEmpty =>
              if (isEmpty)
                userDao.insert(user)
                  .productR(credentialsDao.insert(credentials))
              else
                MonadThrow[G].raiseError[Int] {
                  ResourceConflictException { s"Email already exists: ${Logger.mask(email.address)}" }
                }
            }

        }

    } yield user

}
