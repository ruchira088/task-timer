package com.ruchij.kv

import com.ruchij.kv.codec.decoders.ValueDecoder
import com.ruchij.kv.codec.encoders.{KeyEncoder, ValueEncoder}

trait KeyValueStore[F[_]] {
  type InsertionResult
  type DeletionResult

  def put[K: KeyEncoder[F, *], V: ValueEncoder[F, *]](key: K, value: V): F[InsertionResult]

  def get[K: KeyEncoder[F, *], V: ValueDecoder[F, *]](key: K): F[Option[V]]

  def remove[K: KeyEncoder[F, *]](key: K): F[DeletionResult]
}
