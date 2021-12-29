package com.ruchij.dao.doobie

import cats.{Applicative, ApplicativeError}
import com.ruchij.dao.doobie.exceptions.IllegalDatabaseOperationException
import doobie.ConnectionIO

object DoobieSyntax {

  implicit class DoobieOps(fValue: ConnectionIO[Int]) {
    val single: ConnectionIO[Int] =
      fValue.flatMap { count =>
        if (count == 1) Applicative[ConnectionIO].pure(1)
        else ApplicativeError[ConnectionIO, Throwable].raiseError {
          IllegalDatabaseOperationException {
            s"Expected a single database row to be affected, but $count rows were affected"
          }
        }
      }
  }

}
