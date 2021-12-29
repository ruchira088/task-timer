package com.ruchij.types

import cats.effect.kernel.Sync

import java.util.UUID

trait RandomGenerator[F[_], +A] {
  def generate[B >: A]: F[B]
}

object RandomGenerator {
  def apply[F[_], A](implicit randomGenerator: RandomGenerator[F, A]): RandomGenerator[F, A] = randomGenerator

  implicit def randomGeneratorUUID[F[_]: Sync]: RandomGenerator[F, UUID] =
    new RandomGenerator[F, UUID] {
      override def generate[B >: UUID]: F[B] = Sync[F].delay(UUID.randomUUID())
    }

}
