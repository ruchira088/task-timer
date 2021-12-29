package com.ruchij.config

import com.ruchij.dao.providers.Database
import com.ruchij.services.user.models.Password

case class DatabaseConfiguration(database: Database, url: String, user: String, password: Password)