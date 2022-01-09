package com.ruchij

import org.joda.time.DateTime

import scala.concurrent.duration.FiniteDuration

package object syntax {
  implicit class DateTimeOps(dateTime: DateTime) {
    def + (finiteDuration: FiniteDuration): DateTime = dateTime.plus(finiteDuration.toMillis)
  }
}
