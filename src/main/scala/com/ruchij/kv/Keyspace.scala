package com.ruchij.kv

import com.ruchij.services.authentication.models.AuthenticationToken
import com.ruchij.services.authentication.models.AuthenticationToken.AuthSecret

trait Keyspace[K, V] {
  val name: String
}

object Keyspace {
  implicit case object AuthenticationKeySpace extends Keyspace[AuthSecret, AuthenticationToken] {
    override val name: String = "authentication"
  }
}
