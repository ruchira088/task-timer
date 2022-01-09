package com.ruchij.dao.user

import com.ruchij.dao.doobie.DoobieMappings.dateTimeMeta
import com.ruchij.dao.doobie.DoobieSyntax.DoobieOps
import com.ruchij.dao.user.models.{Email, User}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

object DoobieUserDao extends UserDao[ConnectionIO] {
  val SelectQuery = fr"SELECT id, created_at, first_name, last_name, email FROM users"

  override def insert(user: User): ConnectionIO[Int] =
    sql"""
      INSERT INTO users (id, created_at, first_name, last_name, email)
        VALUES (${user.id}, ${user.createdAt}, ${user.firstName}, ${user.lastName}, ${user.email})
       """.update.run.single

  override def findByEmail(email: Email): ConnectionIO[Option[User]] =
    (SelectQuery ++ fr"WHERE email = ${email.address}").query[User].option

  override def findByUserId(userId: String): ConnectionIO[Option[User]] =
    (SelectQuery ++ fr"WHERE id = $userId").query[User].option
}
