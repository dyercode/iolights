package com.dyercode.iolights

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.{Host, Port}
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.{Router, Server}

object Remote {
  type Switcher = LightStatus => IO[_]

  def helloWorldService(
      switcher: Switcher
  ): HttpRoutes[IO] = {
    HttpRoutes
      // idea, can take an actual message to schedule a time
      .of[IO] { case POST -> Root / "light" / name =>
        name.toLowerCase match {
          case "on"  => Ok(switcher(LightStatus.On).map(_ => "turning on"))
          case "off" => Ok(switcher(LightStatus.Off).map(_ => "turning off"))
          case _     => BadRequest("go fish")
        }
      }
  }

  def httpApp(switcher: Switcher): HttpApp[IO] = Router(
    "/" -> helloWorldService(switcher)
  ).orNotFound

  def serverBuilder(
      conf: ServerConf,
      switcher: Switcher,
  ): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString(conf.host).get)
      .withPort(Port.fromInt(conf.port).get)
      .withHttpApp(httpApp(switcher))
      .build
  }
}
