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
      Task.fork(Ok(PersonDAO.streamPeople.transact(xa).map(p => p.id + "\n").run))(dbExecutor)

    case GET -> Root / "people" =>
      Ok(Task.fork(PersonDAO.listPeople.transact(xa))(dbExecutor))

    case GET -> Root / "people" / IntVar(id) =>
      for {
        person <- Task.fork(PersonDAO.getPerson(id).transact(xa))(dbExecutor)
        result <- person.fold(NotFound())(Ok(_))
      } yield result

    case req @ PUT -> Root / "people" / IntVar(id) =>
      req.decode[PersonForm] { form =>
        Ok(Task.fork(PersonDAO.updatePerson(id, form.firstName, form.familyName).transact(xa))(dbExecutor))
      }

    case req @ POST -> Root / "people" =>
      req.decode[PersonForm] { form =>
        Ok(Task.fork(PersonDAO.insertPerson(form.firstName, form.familyName).transact(xa))(dbExecutor))
      }
  }
}
