import sbt._

object Dependencies
{
  val ScalaVersion = "2.13.7"
  val Http4sVersion = "0.23.7"
  val CirceVersion = "0.14.1"
  val DoobieVersion = "1.0.0-RC1"

  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion

  lazy val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % Http4sVersion

  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion

  lazy val circeGeneric = "io.circe" %% "circe-generic" % CirceVersion

  lazy val circeParser = "io.circe" %% "circe-parser" % CirceVersion

  lazy val circeLiteral = "io.circe" %% "circe-literal" % CirceVersion

  lazy val doobieCore = "org.tpolecat" %% "doobie-core" % DoobieVersion

  lazy val doobieHikari = "org.tpolecat" %% "doobie-hikari" % DoobieVersion

  lazy val h2 = "com.h2database" % "h2" % "2.0.204"

  lazy val postgresql = "org.postgresql" % "postgresql" % "42.3.1"

  lazy val flyway = "org.flywaydb" % "flyway-core" % "8.3.0"

  lazy val enumeratum = "com.beachape" %% "enumeratum" % "1.7.0"

  lazy val bcrypt = "org.mindrot" % "jbcrypt" % "0.4"

  lazy val jodaTime = "joda-time" % "joda-time" % "2.10.13"

  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.17.1"

  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.10"

  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"

  lazy val kindProjector = "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full

  lazy val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % "0.3.1"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.10"

  lazy val scalaMock = "org.scalamock" %% "scalamock" % "5.2.0"

  lazy val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
}
