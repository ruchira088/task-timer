package com.ruchij.dao.providers

import enumeratum.{Enum, EnumEntry}

sealed trait Database extends EnumEntry

object Database extends Enum[Database] {
  case object RDBMS extends Database

  override def values: IndexedSeq[Database] = findValues
}
