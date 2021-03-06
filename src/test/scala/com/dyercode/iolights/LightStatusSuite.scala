package com.dyercode.iolights

import cats.effect._
import cats.implicits._
import com.dyercode.iolights.LightStatus.On
import com.dyercode.iolights.Schedule.{checkScheduleItemTriggered, now}
import cats.effect.unsafe.implicits.global

import java.time.LocalTime

//noinspection ScalaStyle
class LightStatusSuite extends munit.FunSuite {
  test("time") {
    val times = for {
      before <- now[IO]
      after <- now[IO]
    } yield before.isBefore(after)
    assert(times.unsafeRunSync())
  }

  test("schedule to datetime") {
    assertEquals(Schedule.parseTime("8:00").get, LocalTime.of(8, 0))
    assertEquals(Schedule.parseTime("23:33").get, LocalTime.of(23, 33))
  }

  test("a before and an after return some change") {
    val entry = (LocalTime.of(8, 0), On)
    val before = LocalTime.of(7, 0)
    val after = LocalTime.of(9, 0)
    assertEquals(checkScheduleItemTriggered(entry, before, after), Some(On))
  }

  test("a before and an exactly on return some change") {
    val entry = (LocalTime.of(8, 0), On)
    val before = LocalTime.of(7, 0)
    val after = LocalTime.of(8, 0)
    assertEquals(checkScheduleItemTriggered(entry, before, after), Some(On))
  }

  test("two befores return none") {
    val entry = (LocalTime.of(9, 0), On)
    val before = LocalTime.of(7, 0)
    val before2 = LocalTime.of(8, 0)
    assertEquals(checkScheduleItemTriggered(entry, before, before2), None)
  }

  test("two afters return none") {
    val entry = (LocalTime.of(6, 0), On)
    val after = LocalTime.of(7, 0)
    val after2 = LocalTime.of(8, 0)
    assertEquals(checkScheduleItemTriggered(entry, after, after2), None)
  }

  test("still triggers at midnight/on day turnover on midnight") {
    val entry = (LocalTime.of(0, 0), On)
    val before = LocalTime.of(23, 59)
    val after = LocalTime.of(0, 1)
    assertEquals(checkScheduleItemTriggered(entry, before, after), Some(On))
  }

  test("still triggers at midnight/on day turnover pre midnight") {
    val entry = (LocalTime.of(23, 59), On)
    val before = LocalTime.of(23, 58)
    val after = LocalTime.of(0, 1)
    assertEquals(checkScheduleItemTriggered(entry, before, after), Some(On))
  }

  test("still triggers at midnight/on day turnover post midnight") {
    val entry = (LocalTime.of(0, 1), On)
    val before = LocalTime.of(23, 59)
    val after = LocalTime.of(0, 2)
    assertEquals(checkScheduleItemTriggered(entry, before, after), Some(On))
  }

  test("combining off and on, last wins") {
    import LightStatus._
    assertEquals(on |+| on, on)
    assertEquals(off |+| off, off)
    assertEquals(on |+| off, off)
    assertEquals(off |+| on, on)
  }

  test("failed parsing schedule removes it from map") {
    val scheds: Map[String, LightStatus] = Map(
      "9:00" -> LightStatus.On,
      "24:08" -> LightStatus.Off
    )
    assertEquals(
      Schedule.schedule(scheds),
      Map(LocalTime.of(9, 0) -> LightStatus.On)
    )
  }
}
