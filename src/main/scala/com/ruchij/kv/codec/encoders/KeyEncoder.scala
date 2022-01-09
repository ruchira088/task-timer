package com.ruchij.kv.codec.encoders

import cats.implicits._
import cats.{Applicative, Functor}
import shapeless.{::, Generic, HNil}

trait KeyEncoder[F[_], -A] { self =>
  def encode(key: A): F[String]

  def map(f: String => String)(implicit functor: Functor[F]): KeyEncoder[F, A] =
    (key: A) => self.encode(key).map(f)
}

object KeyEncoder {
  def apply[F[_], A](implicit keyEncoder: KeyEncoder[F, A]): KeyEncoder[F, A] = keyEncoder

  implicit def stringKeyEncoder[F[_]: Applicative]: KeyEncoder[F, String] = (key: String) => Applicative[F].pure(key)

  implicit def stringValueClassKeyEncoder[F[_]: Applicative, A](
    implicit generic: Generic.Aux[A, String :: HNil]
  ): KeyEncoder[F, A] = (key: A) => Applicative[F].pure(generic.to(key).head)
}
