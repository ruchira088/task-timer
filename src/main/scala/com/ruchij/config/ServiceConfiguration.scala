package com.ruchij.config

import cats.ApplicativeError
import com.ruchij.types.FunctionKTypes._
import com.ruchij.config.ConfigReaders.dateTimeConfigReader
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

case class ServiceConfiguration(
  httpConfiguration: HttpConfiguration,
  databaseConfiguration: DatabaseConfiguration,
  buildInformation: BuildInformation
)

object ServiceConfiguration {
  def parse[F[_]: ApplicativeError[*[_], Throwable]](configObjectSource: ConfigObjectSource): F[ServiceConfiguration] =
    configObjectSource.load[ServiceConfiguration].left.map(ConfigReaderException.apply).toType[F, Throwable]
}
