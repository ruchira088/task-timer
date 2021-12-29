package com.ruchij.services.hash

trait HashingService[F[_]] {
  def hash(input: String): F[String]
}
