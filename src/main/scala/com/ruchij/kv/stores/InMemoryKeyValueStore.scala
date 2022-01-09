package com.ruchij.kv.stores

import cats.data.OptionT
import cats.implicits._
import cats.effect.kernel.Sync
import com.ruchij.kv.KeyValueStore
import com.ruchij.kv.codec.decoders.ValueDecoder
import com.ruchij.kv.codec.encoders.{KeyEncoder, ValueEncoder}
import io.circe.Json

import java.util.concurrent.ConcurrentHashMap

class InMemoryKeyValueStore[F[_]: Sync] extends KeyValueStore[F] {
  override type InsertionResult = Option[Json]
  override type DeletionResult = Option[Json]

  private val keyValueStore = new ConcurrentHashMap[String, Json]()

  override def put[K: KeyEncoder[F, *], V: ValueEncoder[F, *]](key: K, value: V): F[InsertionResult] =
    for {
      encodedKey <- KeyEncoder[F, K].encode(key)
      encodedValue <- ValueEncoder[F, V].encode(value)

      result <- Sync[F].delay { Option(keyValueStore.put(encodedKey, encodedValue)) }
    }
    yield result

  override def get[K: KeyEncoder[F, *], V: ValueDecoder[F, *]](key: K): F[Option[V]] =
    KeyEncoder[F, K].encode(key)
      .flatMap { encodedKey =>
        OptionT.fromOption[F](Option(keyValueStore.get(encodedKey)))
          .semiflatMap(ValueDecoder[F, V].decode)
          .value
      }

  override def remove[K: KeyEncoder[F, *]](key: K): F[DeletionResult] =
    KeyEncoder[F, K].encode(key)
      .flatMap { encodedKey =>
        Sync[F].delay {
          Option(keyValueStore.remove(encodedKey))
        }
      }
}
