package com.ruchij.config

import com.ruchij.services.user.models.Password
import enumeratum.{Enum, EnumEntry}
import org.joda.time.DateTime
import pureconfig.ConfigReader
import pureconfig.error.{CannotConvert, FailureReason}
import shapeless.{::, Generic, HNil}

import scala.reflect.ClassTag
import scala.util.Try

object ConfigReaders {
  implicit val dateTimeConfigReader: ConfigReader[DateTime] =
    ConfigReader.fromNonEmptyString { input =>
      Try(DateTime.parse(input)).toEither.left.map { throwable =>
        CannotConvert(input, classOf[DateTime].getSimpleName, throwable.getMessage)
      }
    }

  def valueClassConfigReader[A <: AnyVal, T](
    implicit configReader: ConfigReader[T],
    generic: Generic.Aux[A, T :: HNil]
  ): ConfigReader[A] =
    configReader.map(value => generic.from(value :: HNil))

  implicit val passwordConfigReader: ConfigReader[Password] = valueClassConfigReader[Password, String]

  implicit def enumConfigReader[A <: EnumEntry](implicit enumValues: Enum[A], classTag: ClassTag[A]): ConfigReader[A] =
    ConfigReader.fromNonEmptyString[A] { input =>
      enumValues.values
        .find(_.entryName.equalsIgnoreCase(input.trim))
        .fold[Either[FailureReason, A]](
          Left(
            CannotConvert(
              input,
              classTag.runtimeClass.getName,
              s"Possible values are: ${enumValues.values.map(_.entryName).mkString(", ")}"
            )
          )
        ) { enumValue =>
          Right(enumValue)
        }

    }
}
