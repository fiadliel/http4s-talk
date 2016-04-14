package demo

import java.util.concurrent.Executors

import doobie.imports._
import io.circe._
import io.circe.generic.auto._
import io.circe.java8.time._
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

import scalaz.concurrent.Task
import scalaz.stream.Process

object DemoService {
  val dbExecutor: java.util.concurrent.ExecutorService = Executors.newFixedThreadPool(64)
  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = org.http4s.circe.jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = org.http4s.circe.jsonEncoderOf[A]

  def service(xa: Transactor[Task]) = HttpService {
    case GET -> Root / "stream" =>
      Ok(PersonDAO.streamPeople.transact(xa).map(p => p.id + "\n"))

    case GET -> Root / "people" =>
      Ok(PersonDAO.listPeople.transact(xa))

    case GET -> Root / "people" / IntVar(id) =>
      for {
        person <- PersonDAO.getPerson(id).transact(xa)
        result <- person.fold(NotFound())(Ok(_))
      } yield result

    case req @ PUT -> Root / "people" / IntVar(id) =>
      req.decode[PersonForm] { form =>
        Ok(PersonDAO.updatePerson(id, form.firstName, form.familyName).transact(xa))
      }

    case req @ POST -> Root / "people" =>
      req.decode[PersonForm] { form =>
        Ok(PersonDAO.insertPerson(form.firstName, form.familyName).transact(xa))
      }
  }.mapK(Task.fork(_)(dbExecutor))
}
