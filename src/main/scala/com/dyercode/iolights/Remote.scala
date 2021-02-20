package com.dyercode.iolights

import cats.data.Kleisli
import cats.syntax.all._
import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{Router, Server}
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext

object Remote {
  type Switcher[F[_]] = LightStatus => F[_]

  def helloWorldService[F[_]: Sync](switcher: Switcher[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes
      //idea, can take an actual message to schedule a time
      .of[F] { case POST -> Root / "light" / name =>
        name.toLowerCase match {
          case "on"  => Ok(switcher(LightStatus.On).map(_ => "turning on"))
          case "off" => Ok(switcher(LightStatus.Off).map(_ => "turning off"))
          case _     => BadRequest("go fish")
        }
      }
  }

  def httpApp[F[_]: Sync](switcher: Switcher[F]): HttpApp[F] = Router(
    "/" -> helloWorldService(switcher)
  ).orNotFound

  def serverBuilder[F[_]: Sync: ConcurrentEffect](
      concurrentEffect: ExecutionContext,
      conf: ServerConf,
      switcher: Switcher[F]
  )(implicit timer: Timer[F]): Resource[F, Server[F]] = {
    BlazeServerBuilder[F](concurrentEffect)
      .bindHttp(conf.port, conf.host)
      .withHttpApp(httpApp(switcher))
      .resource
  }
}
