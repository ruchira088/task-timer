package com.ruchij.web.requests

import com.ruchij.dao.user.models.Email
import com.ruchij.services.user.models.Password

case class CreateUserRequest(firstName: String, lastName: Option[String], email: Email, password: Password)
