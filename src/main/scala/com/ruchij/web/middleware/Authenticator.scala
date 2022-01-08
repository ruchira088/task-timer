package com.ruchij.web.middleware

import cats.data.{Kleisli, OptionT}
import cats.{ApplicativeError, MonadThrow}
import com.ruchij.exceptions.AuthenticationException
import com.ruchij.services.authentication.AuthenticationService
import com.ruchij.services.authentication.models.AuthContext
import com.ruchij.services.authentication.models.AuthenticationToken.AuthSecret
import org.http4s.Credentials.Token
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthScheme, ContextRequest, Request}

object Authenticator {
  private val AuthenticationCookieName = "authentication"

  def apply[F[_]: MonadThrow](authenticationService: AuthenticationService[F], strict: Boolean): AuthMiddleware[F, AuthContext] =
    authRoutes =>
      Kleisli { request =>
        for {
          authSecret <-
            authSecretFromHeader(request)
              .orElse(authSecretFromCookie(request)) match {
              case None =>
                if (strict) OptionT.liftF(ApplicativeError[F, Throwable].raiseError[AuthSecret](AuthenticationException("Missing authentication token")))
                else OptionT.none[F, AuthSecret]

              case Some(authSecret) => OptionT.pure[F](authSecret)
            }

          authContext <- OptionT.liftF(authenticationService.authenticate(authSecret))

          response <- authRoutes(ContextRequest(authContext, request))
        }
        yield response
    }

  private def authSecretFromHeader[F[_], Req[_[_]]](
    request: Req[F]
  )(implicit requestMapper: Req[F] => Request[F]): Option[AuthSecret] =
    request.headers
      .get[Authorization]
      .collect {
        case Authorization(Token(AuthScheme.Bearer, bearerToken)) => AuthSecret(bearerToken)
      }

  private def authSecretFromCookie[F[_], Req[_[_]]](
    request: Req[F]
  )(implicit requestMapper: Req[F] => Request[F]): Option[AuthSecret] =
    request.cookies.find(_.name == AuthenticationCookieName).map(cookie => AuthSecret(cookie.content))

}
