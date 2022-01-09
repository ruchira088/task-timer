package com.ruchij.kv

import cats.Functor
import com.ruchij.kv.codec.decoders.ValueDecoder
import com.ruchij.kv.codec.encoders.{KeyEncoder, ValueEncoder}

class KeyspacedKeyValueStore[F[_]: Functor, K: KeyEncoder[F, *], V: ValueEncoder[F, *]: ValueDecoder[F, *]](
  val keyValueStore: KeyValueStore[F],
  keyspace: Keyspace[K, V]
) {
  private val keyspaceKeyEncoder = KeyEncoder[F, K].map(key => s"${keyspace.name}-$key")

  def put(key: K, value: V): F[keyValueStore.InsertionResult] =
    keyValueStore.put(key, value)(keyspaceKeyEncoder, ValueEncoder[F, V])

  def get(key: K): F[Option[V]] =
    keyValueStore.get(key)(keyspaceKeyEncoder, ValueDecoder[F, V])

  def delete(key: K): F[keyValueStore.DeletionResult] =
    keyValueStore.remove(key)(keyspaceKeyEncoder)
}
