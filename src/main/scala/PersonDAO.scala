package demo

import java.time.Instant

import doobie.imports._
import scalaz.stream.Process

object PersonDAO {
  implicit val metaInstant =
    Meta[java.sql.Timestamp].nxmap(
      _.toInstant,
      (i: Instant) => new java.sql.Timestamp(i.toEpochMilli)
    )

  val streamPeople: Process[ConnectionIO, Person] =
    sql"select id, first_name, family_name, registered_at from people"
      .query[Person]
      .process

  val listPeople: ConnectionIO[List[Person]] =
    sql"select id, first_name, family_name, registered_at from people"
      .query[Person]
      .list

  def getPerson(id: Long): ConnectionIO[Option[Person]] =
    sql"select id, first_name, family_name, registered_at from people where id = $id"
      .query[Person]
      .option

  def updatePerson(id: Int, firstName: String, familyName: String): ConnectionIO[Person] =
    sql"update people set first_name=$firstName, family_name=$familyName where id=$id"
      .update
      .withUniqueGeneratedKeys("id", "first_name", "family_name", "registered_at")

  def insertPerson(firstName: String, familyName: String, registeredAt: Instant = Instant.now()): ConnectionIO[Person] =
    sql"insert into people (first_name, family_name, registered_at) values ($firstName, $familyName, $registeredAt)"
      .update
      .withUniqueGeneratedKeys("id", "first_name", "family_name", "registered_at")
}
