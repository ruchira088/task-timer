package com.ruchij

import cats.MonadThrow
import cats.effect.kernel.Resource
import cats.effect.{Async, ExitCode, IO, IOApp}
import cats.implicits._
import com.ruchij.config.{BuildInformation, DatabaseConfiguration, ServiceConfiguration}
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
        program[IO](serviceConfiguration.databaseConfiguration, serviceConfiguration.buildInformation).use { httpApp =>
          BlazeServerBuilder[IO]
            .withHttpApp(httpApp)
            .bindHttp(serviceConfiguration.httpConfiguration.port, serviceConfiguration.httpConfiguration.host)
            .serve.compile.drain
        }

    }
    yield ExitCode.Success

  def program[F[_]: Async: RandomGenerator[*[_], UUID]](databaseConfiguration: DatabaseConfiguration, buildInformation: BuildInformation): Resource[F, HttpApp[F]] =
    databaseConfiguration.transactor.evalMap { implicit transactor =>
      implicit val monadThrow: MonadThrow[databaseConfiguration.G] = databaseConfiguration.monadThrow

      for {
        userDao <- databaseConfiguration.userDaoF[F]
        credentialsDao <- databaseConfiguration.credentialsDaoF[F]

        hashingService = new BcryptHashingService[F]

        userService = new UserServiceImpl[F, databaseConfiguration.G](hashingService, userDao, credentialsDao)
        healthService = new HealthServiceImpl[F](buildInformation)
      }
      yield Routes(userService, healthService)
    }
}
