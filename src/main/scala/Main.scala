package demo

import doobie.imports._
import org.http4s._
import org.http4s.server.blaze._

import scalaz.concurrent.Task

object Main {
  def main(args: Array[String]): Unit = {
    val xa = DriverManagerTransactor[Task](
      "org.postgresql.Driver", "jdbc:postgresql:demo", "demo", ""
    )

    val server =
      BlazeBuilder
        .bindHttp(8080)
        .mountService(DemoService.service(xa))
        .run

    server.awaitShutdown()
  }
}
