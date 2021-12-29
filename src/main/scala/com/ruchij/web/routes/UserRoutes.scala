package com.ruchij.web.routes

import cats.effect.Async
import cats.implicits._
import com.ruchij.circe.Encoders.{dateTimeEncoder, emailEncoder}
import com.ruchij.circe.Decoders.{emailDecoder, passwordDecoder}
import com.ruchij.services.user.UserService
import com.ruchij.web.requests.CreateUserRequest
import com.ruchij.web.requests.RequestOps
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object UserRoutes {

  def apply[F[_]: Async](userService: UserService[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    HttpRoutes.of {
      case request @ POST -> Root =>
        for {
          CreateUserRequest(firstName, lastName, email, password) <- request.to[CreateUserRequest]
          user <- userService.create(firstName, lastName, email, password)
          response <- Created(user)
        }
        yield response
    }

  }

}
