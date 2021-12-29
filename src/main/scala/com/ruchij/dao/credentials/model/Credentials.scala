package com.ruchij.dao.credentials.model

import org.joda.time.DateTime

case class Credentials(userId: String, createdAt: DateTime, hashedPassword: String)
