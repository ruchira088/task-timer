package com.ruchij.dao.providers

import cats.effect.Async
import cats.effect.kernel.{Resource, Sync}
import cats.{Applicative, MonadThrow, ~>}
import com.ruchij.dao.credentials.{CredentialsDao, DoobieCredentialsDao}
import com.ruchij.dao.user.{DoobieUserDao, UserDao}
import com.ruchij.services.user.models.Password
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway

class DoobieDaoProvider[F[_]: Async](url: String, user: String, password: Password) extends DaoProvider[F] {
  override type G[x] = ConnectionIO[x]

  override val monadThrow: MonadThrow[ConnectionIO] = MonadThrow[ConnectionIO]

  override def userDaoF: F[UserDao[ConnectionIO]] =
    Applicative[F].pure(DoobieUserDao)

  override def credentialsDaoF: F[CredentialsDao[ConnectionIO]] =
    Applicative[F].pure(DoobieCredentialsDao)

  override def transactor: Resource[F, ConnectionIO ~> F] =
    for {
      migrationResult <- Resource.eval {
        Sync[F].blocking {
          Flyway.configure().dataSource(url, user, password.value).load()
            .migrate()
        }
      }

      connectEC <- ExecutionContexts.fixedThreadPool(8)

      hikariTransactor <- HikariTransactor.newHikariTransactor(
        classOf[org.h2.Driver].getName,
        url,
        user,
        password.value,
        connectEC
      )
    } yield hikariTransactor.trans
}
