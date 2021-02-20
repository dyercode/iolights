package com.dyercode.iolights

import cats.effect.implicits._
import cats.effect.{
  Async,
  ConcurrentEffect,
  ExitCode,
  IO,
  IOApp,
  Resource,
  Sync,
  Timer
}
import cats.implicits._
import com.dyercode.iolights.LightStatus.{Off, On}
import com.dyercode.pi4jsw.Gpio
import com.pi4j.io.gpio.{GpioController, GpioPinDigitalOutput, RaspiPin}
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext.global
import scala.io.StdIn

//noinspection ScalaStyle
object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- IO(println("Enter to toggle. 'exit' to exit"))
      conf <- IO(ConfigSource.default.load[ServerConf])
      exit <- conf match {
        case Right(sc) => program[IO](sc)
        case Left(l)   => IO.raiseError(new RuntimeException(l.prettyPrint()))
      }
    } yield exit
  }

  def loop[F[_]: Sync](
      pin: GpioPinDigitalOutput
  )(implicit timer: Timer[F]): F[ExitCode] = {
    def loopInner[G[_]: Sync](
        input: String
    )(implicit timer: Timer[G]): G[ExitCode] = {
      if (input == "exit") {
        Sync[G].delay(ExitCode.Success)
      } else {
        for {
          _ <- Sync[G].delay(pin.toggle())
          r <- loop[G](pin)
        } yield r
      }
    }

    for {
      _ <- Sync[F].delay(
        println(s"light is ${if (pin.isHigh) "Off" else "On"}")
      )
      input <- Sync[F].delay(StdIn.readLine())
      result <- loopInner[F](input)
    } yield result
  }

  def makeGpioController[F[_]: Sync]: Resource[F, GpioController] = {
    Resource.make(Gpio.initialize[F])(Gpio.shutdown[F])
  }

  def program[F[_]: Async: ConcurrentEffect](conf: ServerConf)(implicit
      timer: Timer[F]
  ): F[ExitCode] = {
    makeGpioController[F].use { gpioController =>
      for {
        light <- Gpio.provision[F](
          gpioController,
          RaspiPin.GPIO_25,
          "light",
          None
        )
        turnOn = Sync[F].delay {
          println("ouch ON"); light.low()
        }
        turnOff = Sync[F].delay {
          println("ouch OFF"); light.high()
        }
        switcher = { ls: LightStatus =>
          ls match {
            case On  => turnOn
            case Off => turnOff
          }
        }
        _ <- Schedule
          .loop[F](switcher)
          .start
        listenFiber <- Remote
          .serverBuilder[F](global, conf, switcher)
          .use(_ => never)
          .start
        exit <- loop(light)
        _ <- listenFiber.cancel
      } yield exit
    }
  }

  private def never[F[_]: Async: ConcurrentEffect]: F[Unit] = {
    Async[F].async { (_: Either[Throwable, Unit] => Unit) => () }
  }
}
