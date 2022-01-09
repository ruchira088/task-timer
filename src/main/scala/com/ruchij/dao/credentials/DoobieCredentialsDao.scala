package com.ruchij.dao.credentials

import com.ruchij.dao.credentials.model.Credentials
import com.ruchij.dao.doobie.DoobieMappings.dateTimeMeta
import com.ruchij.dao.doobie.DoobieSyntax.DoobieOps
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

object DoobieCredentialsDao extends CredentialsDao[ConnectionIO] {

  val SelectQuery = fr"SELECT user_id, created_at, hashed_password"

  override def insert(credentials: Credentials): ConnectionIO[Int] =
    sql"""
      INSERT INTO credentials (user_id, created_at, hashed_password)
        VALUES (${credentials.userId}, ${credentials.createdAt}, ${credentials.hashedPassword})
    """
      .update
      .run
      .single

  override def findByUserId(userId: String): ConnectionIO[Option[Credentials]] =
    (SelectQuery ++ fr"WHERE user_id = $userId").query[Credentials].option
}
