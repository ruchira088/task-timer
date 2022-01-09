package com.ruchij.kv.codec.decoders

import cats.ApplicativeError
import com.ruchij.types.FunctionKTypes._
import io.circe.{Decoder, Json}

trait ValueDecoder[F[_], +A] {
  def decode[B >: A](input: Json): F[B]
}

object ValueDecoder {
  def apply[F[_], A](implicit valueDecoder: ValueDecoder[F, A]): ValueDecoder[F, A] = valueDecoder

  implicit def circeValueDecoder[F[_]: ApplicativeError[*[_], Throwable], A](implicit decoder: Decoder[A]): ValueDecoder[F, A] =
    new ValueDecoder[F, A] {
      override def decode[B >: A](input: Json): F[B] =
        decoder(input.hcursor).map(identity[B]).toType[F, Throwable]
    }
}
