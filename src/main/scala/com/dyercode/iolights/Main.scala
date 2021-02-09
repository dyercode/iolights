package com.dyercode.iolights

import cats.effect.{Concurrent, ExitCode, IO, IOApp, Sync, Timer}
import cats.implicits._
import cats.effect.implicits._
import com.dyercode.iolights.LightStatus.{Off, On}
import com.dyercode.pi4jsw.Gpio
import com.pi4j.io.gpio.{GpioPinDigitalOutput, PinState, RaspiPin}

import scala.io.StdIn

//noinspection ScalaStyle
object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- IO(println("Enter to toggle. 'exit' to exit"))
      exit <- program[IO]
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

  def program[F[_]: Concurrent](implicit timer: Timer[F]): F[ExitCode] = {
    (for {
      gpio <- Gpio.initialize[F]
      light <- Gpio.provision[F](gpio, RaspiPin.GPIO_25, "light", PinState.LOW)
      _ <- Schedule
        .loop[F](
          None,
          None,
          {
            case On  => Sync[F].delay({ println("ouch ON"); light.low() })
            case Off => Sync[F].delay({ println("ouch OFF"); light.high() })
          }
        )
        .start
      exit <- loop(light)
      _ <- Gpio.shutdown[F](gpio)
    } yield exit)
      .handleErrorWith { _ =>
        Gpio.shutdown[F].map(_ => ExitCode.Error)
      }
  }
}
