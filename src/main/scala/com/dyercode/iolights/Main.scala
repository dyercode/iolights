package com.dyercode.iolights

import cats.effect._
import com.dyercode.iolights.LightStatus.{Off, On}
import com.dyercode.pi4jsw.Gpio
import com.pi4j.io.gpio.{GpioController, GpioPinDigitalOutput, RaspiPin}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- IO.println("Enter to toggle. 'exit' to exit")
      conf <- Conf.load
      exit <- program(conf)
    } yield exit
  }

  def loop(pin: GpioPinDigitalOutput): IO[ExitCode] = {
    def loopInner(input: String): IO[ExitCode] = {
      if (input == "exit") {
        IO(ExitCode.Success)
      } else {
        for {
          _ <- IO(pin.toggle())
          r <- loop(pin)
        } yield r
      }
    }

    for {
      _ <- IO.println(s"light is ${if (pin.isHigh) "Off" else "On"}")
      input <- IO.readLine
      result <- loopInner(input)
    } yield result
  }

  def makeGpioController[F[_]: Sync]: Resource[F, GpioController] = {
    Resource.make(Gpio.initialize[F])(Gpio.shutdown[F])
  }

  def program(conf: Conf): IO[ExitCode] = {
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
        exit <- loop(light)
        _ <- listenFiber.cancel
      } yield exit
    }
  }
}
