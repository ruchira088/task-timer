package com.ruchij.kv.codec.encoders

import cats.Applicative
import io.circe.{Encoder, Json}

trait ValueEncoder[F[_], -A] {
  def encode(value: A): F[Json]
}

object ValueEncoder {
  def apply[F[_], A](implicit valueEncoder: ValueEncoder[F, A]): ValueEncoder[F, A] = valueEncoder

  implicit def circeValueEncoder[F[_]: Applicative, A](implicit encoder: Encoder[A]): ValueEncoder[F, A] =
    (value: A) => Applicative[F].pure(encoder(value))
}
