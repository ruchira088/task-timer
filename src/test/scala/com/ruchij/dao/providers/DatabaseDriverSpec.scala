package com.ruchij.dao.providers

import com.ruchij.dao.providers.DoobieDaoProvider.DatabaseDriver
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class DatabaseDriverSpec extends AnyFlatSpec with Matchers {

  "fromUrl" should "infer the database driver from the URL" in {
    val h2Url = "jdbc:h2:mem:task-timer;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
    val postgresqlUrl = "jdbc:postgresql://postgres:5432/task-timer"

    DatabaseDriver.fromUrl(h2Url) mustBe Some(DatabaseDriver.H2)
    DatabaseDriver.fromUrl(postgresqlUrl) mustBe Some(DatabaseDriver.Postgresql)
  }

}
