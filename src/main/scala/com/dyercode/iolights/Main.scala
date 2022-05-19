package com.dyercode.iolights

import cats.effect.*
import com.dyercode.iolights.LightStatus.{Off, On}
import com.dyercode.pi4jsw.Gpio
import com.pi4j.io.gpio.{GpioController, GpioPinDigitalOutput, RaspiPin}

object Main extends IOApp.Simple {
  override def run: IO[Unit] =
    for {
      _ <- IO.println("Enter to toggle. 'exit' to exit")
      conf <- Conf.load
      _ <- program(conf)
    } yield ()

  def loop(pin: GpioPinDigitalOutput): IO[Unit] = {
    def loopInner(input: String): IO[Unit] = {
      if (input == "exit") {
        IO.unit
      } else {
        IO(pin.toggle())
          .flatMap(_ => loop(pin))
      }
    }

    for {
      _ <- IO.println(s"light is ${if pin.isHigh then "Off" else "On"}")
      input <- IO.readLine
      result <- loopInner(input)
    } yield result
  }

  def makeGpioController[F[_]: Sync]: Resource[F, GpioController] =
    Resource.make(Gpio.initialize[F])(Gpio.shutdown[F])

  def program(conf: Conf): IO[Unit] = {
    makeGpioController[IO].use { gpioController =>
      for {
        schedule <- IO(Schedule(conf))
        light <- Gpio.provision[IO](
          gpioController,
          RaspiPin.GPIO_25,
          "light",
          None,
        )
        turnOn = IO.println("ouch ON").flatMap(_ => IO(light.low()))
        turnOff = IO.println("ouch OFF").flatMap(_ => IO(light.high()))
        switcher = (_: LightStatus) match {
          case On  => turnOn
          case Off => turnOff
        }
        _ <- schedule
          .loop(switcher)
          .start
        listenFiber <- Remote
          .serverBuilder(conf.server, switcher)
          .use(_ => IO.never)
          .start
        _ <- loop(light)
        _ <- listenFiber.cancel
      } yield ()
    }
  }
}
