package demo

import java.time.Instant

// create table people (id serial primary key, first_name text, family_name text, registered_at timestamp with time zone);
case class Person(id: Int, firstName: String, familyName: String, registeredAt: Instant)
