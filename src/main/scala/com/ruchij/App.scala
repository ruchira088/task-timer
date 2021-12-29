package com.ruchij

import cats.MonadThrow
import cats.effect.kernel.Resource
import cats.effect.{Async, ExitCode, IO, IOApp}
import cats.implicits._
import com.ruchij.config.{BuildInformation, ServiceConfiguration}
import com.ruchij.dao.providers.DaoProvider
import com.ruchij.services.hash.BcryptHashingService
import com.ruchij.services.health.HealthServiceImpl
import com.ruchij.services.user.UserServiceImpl
import com.ruchij.types.RandomGenerator
import com.ruchij.web.Routes
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import pureconfig.ConfigSource

import java.util.UUID

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      serviceConfiguration <- ServiceConfiguration.parse[IO](configObjectSource)

      _ <-
        program[IO](DaoProvider.from(serviceConfiguration.databaseConfiguration), serviceConfiguration.buildInformation)
          .use { httpApp =>
            BlazeServerBuilder[IO]
              .withHttpApp(httpApp)
              .bindHttp(serviceConfiguration.httpConfiguration.port, serviceConfiguration.httpConfiguration.host)
              .serve.compile.drain
          }

    }
    yield ExitCode.Success

  def program[F[_]: Async: RandomGenerator[*[_], UUID]](daoProvider: DaoProvider[F], buildInformation: BuildInformation): Resource[F, HttpApp[F]] =
    daoProvider.transactor.evalMap { implicit transactor =>
      implicit val monadThrow: MonadThrow[daoProvider.G] = daoProvider.monadThrow

      for {
        userDao <- daoProvider.userDaoF
        credentialsDao <- daoProvider.credentialsDaoF

        hashingService = new BcryptHashingService[F]

        userService = new UserServiceImpl[F, daoProvider.G](hashingService, userDao, credentialsDao)
        healthService = new HealthServiceImpl[F](buildInformation)
      }
      yield Routes(userService, healthService)
    }
}
