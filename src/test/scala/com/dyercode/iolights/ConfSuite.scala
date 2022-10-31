package com.dyercode.iolights

import cats.effect.unsafe.implicits.global
import com.typesafe.config.ConfigFactory
import pureconfig.ConfigSource

class ConfSuite extends munit.FunSuite {
  val source: String = """server {
                  |  host = "localhost"
                  |  port = 8080
                  |}
                  |schedule-file = "schedule.csv"
                  """.stripMargin

  test("conf includes schedule file location") {
    val conf = Conf.load(ConfigSource.string(source)).unsafeRunSync()
    assertEquals(conf.scheduleFile, "schedule.csv")
  }
}
