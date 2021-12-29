package com.ruchij.dao.providers

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.{MonadThrow, ~>}
import com.ruchij.config.DatabaseConfiguration
import com.ruchij.dao.credentials.CredentialsDao
import com.ruchij.dao.user.UserDao

trait DaoProvider[F[_]] {
  type G[x]

  val monadThrow: MonadThrow[G]

  def userDaoF: F[UserDao[G]]
  def credentialsDaoF: F[CredentialsDao[G]]

  def transactor: Resource[F, G ~> F]
}

object DaoProvider {
  def from[F[_]: Async](databaseConfiguration: DatabaseConfiguration): DaoProvider[F] =
    databaseConfiguration.database match {
      case Database.RDBMS =>
        new DoobieDaoProvider[F](databaseConfiguration.url, databaseConfiguration.user, databaseConfiguration.password)
    }

}