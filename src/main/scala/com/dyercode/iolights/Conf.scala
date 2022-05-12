package com.dyercode.iolights

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}

case class ServerConf(
    host: String,
    port: Int,
)

case class Conf(
    server: ServerConf,
    scheduleFile: String,
)

object Conf {
  def load: IO[Conf] = IO(ConfigFactory.load).flatMap(load)

  def load(config: Config): IO[Conf] = for {
    server <- IO(config.getConfig("server"))
    serverConf <- IO {
      ServerConf(server.getString("host"), server.getInt("port"))
    }
    scheduleFile <- IO(config.getString("schedule-file"))
  } yield Conf(serverConf, scheduleFile)
}
