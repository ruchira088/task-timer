package com.ruchij.dao.user.models

import org.joda.time.DateTime

case class User(id: String, createdAt: DateTime, firstName: String, lastName: Option[String], email: Email)