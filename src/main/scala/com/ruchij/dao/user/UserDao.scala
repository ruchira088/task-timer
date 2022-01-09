package com.ruchij.dao.user

import com.ruchij.dao.user.models.{Email, User}

trait UserDao[F[_]] {
  def insert(user: User): F[Int]

  def findByEmail(email: Email): F[Option[User]]

  def findByUserId(userId: String): F[Option[User]]
}
