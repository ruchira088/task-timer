package com.ruchij.dao.doobie

import doobie.implicits.javasql.TimestampMeta
import doobie.util.meta.Meta
import org.joda.time.{DateTime, DateTimeZone}

import java.sql.Timestamp

object DoobieMappings {

  implicit val dateTimeMeta: Meta[DateTime] =
    Meta[Timestamp]
      .timap(timestamp => new DateTime(timestamp.getTime, DateTimeZone.UTC)) {
        dateTime =>new Timestamp(dateTime.getMillis)
      }

}
