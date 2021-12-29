package com.ruchij.dao.credentials

import com.ruchij.dao.credentials.model.Credentials

trait CredentialsDao[F[_]] {

  def insert(credentials: Credentials): F[Int]

}
