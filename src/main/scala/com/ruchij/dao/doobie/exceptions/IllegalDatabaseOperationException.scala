package com.ruchij.dao.doobie.exceptions

case class IllegalDatabaseOperationException(message: String) extends Exception(message)
