package com.ruchij.services.hash

import cats.effect.kernel.Sync
import org.mindrot.jbcrypt.BCrypt

class BcryptHashingService[F[_]: Sync] extends HashingService[F] {

  override def hash(input: String): F[String] =
    Sync[F].delay {
      BCrypt.hashpw(input, BCrypt.gensalt())
    }

}
