package com.ruchij.circe

import com.ruchij.dao.user.models.Email
import io.circe.Encoder
import org.joda.time.DateTime
import shapeless.{::, Generic, HNil}

object Encoders {
  implicit val dateTimeEncoder: Encoder[DateTime] = Encoder.encodeString.contramap[DateTime](_.toString)

  def valueClassEncoder[A <: AnyVal, B](implicit encoder: Encoder[B], generic: Generic.Aux[A, B :: HNil]): Encoder[A] =
    encoder.contramap(value => generic.to(value).head)

  implicit val emailEncoder: Encoder[Email] = valueClassEncoder[Email, String]
}
