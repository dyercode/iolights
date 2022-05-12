package com.dyercode.iolights

import cats.effect.*
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import com.dyercode.iolights.LightStatus.On
import com.dyercode.iolights.Schedule.now

import java.time.LocalTime
import java.util.Random

class LightStatusSuite extends munit.FunSuite {
  val config: Conf = Conf(ServerConf("", 0), "")
  val schedule: Schedule = Schedule(config)
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
    assertEquals(
      schedule.checkScheduleItemTriggered(entry, before, after),
      Some(On),
    )
  }

  test("a before and an exactly on return some change") {
    val entry = (LocalTime.of(8, 0), On)
    val before = LocalTime.of(7, 0)
    val after = LocalTime.of(8, 0)
    assertEquals(
      schedule.checkScheduleItemTriggered(entry, before, after),
      Some(On),
    )
  }

  test("two befores return none") {
    val entry = (LocalTime.of(9, 0), On)
    val before = LocalTime.of(7, 0)
    val before2 = LocalTime.of(8, 0)
    assertEquals(
      schedule.checkScheduleItemTriggered(entry, before, before2),
      None,
    )
  }

  test("two afters return none") {
    val entry = (LocalTime.of(6, 0), On)
    val after = LocalTime.of(7, 0)
    val after2 = LocalTime.of(8, 0)
    assertEquals(
      schedule.checkScheduleItemTriggered(entry, after, after2),
      None,
    )
  }

  test("still triggers at midnight/on day turnover on midnight") {
    val entry = (LocalTime.of(0, 0), On)
    val before = LocalTime.of(23, 59)
    val after = LocalTime.of(0, 1)
    assertEquals(
      schedule.checkScheduleItemTriggered(entry, before, after),
      Some(On),
    )
  }

  test("still triggers at midnight/on day turnover pre midnight") {
    val entry = (LocalTime.of(23, 59), On)
    val before = LocalTime.of(23, 58)
    val after = LocalTime.of(0, 1)
    assertEquals(
      schedule.checkScheduleItemTriggered(entry, before, after),
      Some(On),
    )
  }

  test("still triggers at midnight/on day turnover post midnight") {
    val entry = (LocalTime.of(0, 1), On)
    val before = LocalTime.of(23, 59)
    val after = LocalTime.of(0, 2)
    assertEquals(
      schedule.checkScheduleItemTriggered(entry, before, after),
      Some(On),
    )
  }

  test("combining off and on, last wins") {
    import LightStatus.*
    assertEquals(on |+| on, on)
    assertEquals(off |+| off, off)
    assertEquals(on |+| off, off)
    assertEquals(off |+| on, on)
  }

  test("failed parsing schedule removes it from map") {
    val scheds = List(
      List("9:00", "on"),
      List("24:08", "off"),
    )
    assertEquals(
      schedule.parseSchedule(scheds),
      Map(LocalTime.of(9, 0) -> LightStatus.On),
    )
  }

  test("lightStatus from string") {
    val r: Random = new Random()
    def randomCase(string: String) = string.map { c =>
      if (r.nextBoolean()) {
        c.toUpper
      } else {
        c.toLower
      }
    }

    assertEquals(LightStatus(randomCase("on")), Some(LightStatus.On))
    assertEquals(LightStatus(randomCase("off")), Some(LightStatus.Off))
    assertEquals(LightStatus("o n"), None)
  }
}
