package com.ruchij.services.authentication.models

import com.ruchij.dao.user.models.User

case class AuthContext(user: User, authToken: AuthenticationToken)
