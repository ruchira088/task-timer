package com.ruchij.dao.providers

import cats.effect.Async
import cats.effect.kernel.{Resource, Sync}
import cats.{Applicative, ApplicativeError, MonadThrow, ~>}
import com.ruchij.dao.credentials.{CredentialsDao, DoobieCredentialsDao}
import com.ruchij.dao.providers.DoobieDaoProvider.DatabaseDriver
import com.ruchij.dao.user.{DoobieUserDao, UserDao}
import com.ruchij.services.user.models.Password
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import enumeratum.{Enum, EnumEntry}
import org.flywaydb.core.Flyway
import org.{h2, postgresql}

import java.sql
import scala.reflect.ClassTag
import scala.util.matching.Regex

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

      databaseDriver <- Resource.eval(DatabaseDriver.fromUrlF[F](url))

      connectEC <- ExecutionContexts.fixedThreadPool(8)

      hikariTransactor <- HikariTransactor.newHikariTransactor(
        databaseDriver.className,
        url,
        user,
        password.value,
        connectEC
      )
    } yield hikariTransactor.trans
}

object DoobieDaoProvider {
  sealed abstract class DatabaseDriver[A <: sql.Driver](implicit classTag: ClassTag[A]) extends EnumEntry {
    val className: String = classTag.runtimeClass.getName
  }

  object DatabaseDriver extends Enum[DatabaseDriver[_]] {
    case object H2 extends DatabaseDriver[h2.Driver]
    case object Postgresql extends DatabaseDriver[postgresql.Driver]

    private val JdbcDriver: Regex = "^jdbc:([^:]+):.*".r

    override def values: IndexedSeq[DatabaseDriver[_]] = findValues

    def fromUrl(url: String): Option[DatabaseDriver[_]] =
      url match {
        case JdbcDriver(driver) => values.find(_.entryName.equalsIgnoreCase(driver))
        case _ => None
      }

    def fromUrlF[F[_]: ApplicativeError[*[_], Throwable]](url: String): F[DatabaseDriver[_]] =
      fromUrl(url) match {
        case Some(driver) => Applicative[F].pure(driver)

        case None =>
          ApplicativeError[F, Throwable].raiseError {
            new IllegalArgumentException(s"Unable to infer database driver from $url")
          }
      }
  }

}
