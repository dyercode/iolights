package com.dyercode.iolights

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.{Host, Port}

import io.circe.generic.auto.*
import io.circe.syntax.*

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.{Router, Server}
import org.http4s.circe.*
// import
// import org.http4s.circe.CirceEntityDecoder.*

object Remote {
  type Switcher = LightStatus => IO[?]
  type Status = () => IO[LightStatus]

  case class LightCommand(active: Boolean)
  object LightCommand {
    def fromLightStatus(status: LightStatus) = status match {
      case LightStatus.On  => LightCommand(true)
      case LightStatus.Off => LightCommand(false)
    }
  }

  implicit val commandDecoder: EntityDecoder[IO, LightCommand] =
    jsonOf[IO, LightCommand]

  def restService(
      switcher: Switcher,
      status: Status,
  ): HttpRoutes[IO] = {
    HttpRoutes
      // idea, can take an actual message to schedule a time
      .of[IO] {
        case req @ POST -> Root / "light" => {
          for {
            cmd <- req.as[LightCommand]
            resp <- cmd.active match {
              case true => Ok(switcher(LightStatus.On).map(_ => "turning on"))
              case false =>
                Ok(switcher(LightStatus.Off).map(_ => "turning off"))
            }
          } yield resp
        }
        case POST -> Root / "light" / name =>
          name.toLowerCase match {
            case "on"  => Ok(switcher(LightStatus.On).map(_ => "turning on"))
            case "off" => Ok(switcher(LightStatus.Off).map(_ => "turning off"))
            case _     => BadRequest("go fish")
          }
        case GET -> Root / "light" =>
          Ok(status().map(ls => LightCommand.fromLightStatus(ls).asJson))
      }
  }

  def httpApp(switcher: Switcher, status: Status): HttpApp[IO] =
    Router(
      "/" -> restService(switcher, status)
    ).orNotFound

  def serverBuilder(
      conf: ServerConf,
      switcher: Switcher,
      status: Status,
  ): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString(conf.host).get)
      .withPort(Port.fromInt(conf.port).get)
      .withHttpApp(httpApp(switcher, status))
      .build
  }
}
