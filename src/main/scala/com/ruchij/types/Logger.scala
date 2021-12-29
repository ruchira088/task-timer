package com.ruchij.types

import cats.effect.kernel.Sync
import com.typesafe.scalalogging.{Logger => TypesafeLogger}

import scala.reflect.ClassTag

case class Logger[A: ClassTag](typesafeLogger: TypesafeLogger) {

  def info[F[_]: Sync](infoMessage: String): F[Unit] =
    Sync[F].blocking {
      typesafeLogger.info(infoMessage)
    }

  def warn[F[_]: Sync](warnMessage: String): F[Unit] =
    Sync[F].blocking {
      typesafeLogger.warn(warnMessage)
    }

  def error[F[_]: Sync](errorMessage: String): F[Unit] =
    Sync[F].blocking {
      typesafeLogger.error(errorMessage)
    }

  def error[F[_]: Sync](errorMessage: String, throwable: Throwable): F[Unit] =
    Sync[F].blocking {
      typesafeLogger.error(errorMessage, throwable)
    }

  def debug[F[_]: Sync](debugMessage: String): F[Unit] =
    Sync[F].blocking {
      typesafeLogger.debug(debugMessage)
    }

  def trace[F[_]: Sync](traceMessage: String): F[Unit] =
    Sync[F].blocking {
      typesafeLogger.trace(traceMessage)
    }

}

object Logger {
  def apply[A: ClassTag]: Logger[A] = Logger { TypesafeLogger[A] }

  def mask(data: String): String =
    data.length match {
      case small if small <= 4 => List.fill(small - 1)("*").mkString + data.takeRight(1).mkString
      case medium if medium <= 12 => data.take(2).mkString + List.fill(medium - 4)("*").mkString + data.takeRight(2).mkString
      case large => data.take(4).mkString + List.fill(large - 8)("*").mkString + data.takeRight(4).mkString
    }
}
