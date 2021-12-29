package com.ruchij.circe

import com.ruchij.dao.user.models.Email
import com.ruchij.services.user.models.Password
import io.circe.Decoder
import org.joda.time.DateTime
import shapeless.{::, Generic, HNil}

import scala.util.Try

object Decoders {
  implicit val dateTimeDecoder: Decoder[DateTime] =
    Decoder.decodeString.emapTry(dateTimeString => Try(DateTime.parse(dateTimeString)))

  def valueClassDecoder[A <: AnyVal, B](implicit decoder: Decoder[B], generic: Generic.Aux[A, B :: HNil]): Decoder[A] =
    decoder.map(result => generic.from(result :: HNil))

  implicit val emailDecoder: Decoder[Email] = valueClassDecoder[Email, String]

  implicit val passwordDecoder: Decoder[Password] = valueClassDecoder[Password, String]
}
