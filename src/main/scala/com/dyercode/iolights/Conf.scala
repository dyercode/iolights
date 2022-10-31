package com.dyercode.iolights

import cats.effect.IO
import pureconfig._
import pureconfig.generic.derivation.default._

case class ServerConf(
    host: String,
    port: Int,
) derives ConfigReader

case class Conf(
    server: ServerConf,
    scheduleFile: String,
) derives ConfigReader

object Conf {
  def load: IO[Conf] = IO(ConfigSource.default).flatMap(load)

  def load(config: ConfigObjectSource): IO[Conf] = IO.fromEither {
    config.load[Conf].left.map { failures =>
      java.lang.RuntimeException(failures.prettyPrint(2))
    }
  }
}
