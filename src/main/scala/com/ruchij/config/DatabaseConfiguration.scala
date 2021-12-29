package com.ruchij.config

import cats.effect.Async
import cats.effect.kernel.{Resource, Sync}
import cats.{Applicative, MonadThrow, ~>}
import com.ruchij.dao.credentials.{CredentialsDao, DoobieCredentialsDao}
import com.ruchij.dao.user.{DoobieUserDao, UserDao}
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway
import pureconfig.ConfigReader
import pureconfig.generic.auto._

sealed trait DatabaseConfiguration {
  type G[x]

  val monadThrow: MonadThrow[G]

  val url: String
  val user: String
  val password: String

  def userDaoF[F[_]: Sync]: F[UserDao[G]]
  def credentialsDaoF[F[_]: Sync]: F[CredentialsDao[G]]

  def transactor[F[_]: Async]: Resource[F, G ~> F]
}

object DatabaseConfiguration {
  implicit val databaseConfigurationConfigReader: ConfigReader[DatabaseConfiguration] =
    ConfigReader[DoobieDatabaseConfiguration].map(identity[DatabaseConfiguration])

  case class DoobieDatabaseConfiguration(url: String, user: String, password: String) extends DatabaseConfiguration {

    override type G[x] = ConnectionIO[x]
    override val monadThrow: MonadThrow[ConnectionIO] = Sync[ConnectionIO]

    override def userDaoF[F[_]: Sync]: F[UserDao[ConnectionIO]] =
      Applicative[F].pure(DoobieUserDao)

    override def credentialsDaoF[F[_]: Sync]: F[CredentialsDao[ConnectionIO]] =
      Applicative[F].pure(DoobieCredentialsDao)

    override def transactor[F[_]: Async]: Resource[F, ConnectionIO ~> F] =
      for {
        migrationResult <- Resource.eval {
          Sync[F].blocking {
            Flyway.configure().dataSource(url, user, password).load()
              .migrate()
          }
        }

        connectEC <- ExecutionContexts.fixedThreadPool(8)

        hikariTransactor <- HikariTransactor.newHikariTransactor(
          classOf[org.h2.Driver].getName,
          url,
          user,
          password,
          connectEC
        )
      } yield hikariTransactor.trans
  }
}
