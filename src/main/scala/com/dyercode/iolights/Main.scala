package com.dyercode.iolights

import cats.effect.{ExitCode, IO, IOApp}
import com.pi4j.io.gpio.{
  GpioController,
  GpioPinDigitalOutput,
  PinState,
  RaspiPin
}
import scala.io.StdIn

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- IO(println("Enter to toggle. 'exit' to exit"))
      gpio <- Gpio.initialize()
      exit <- program(gpio)
    } yield exit
  }

  def loop(pin: GpioPinDigitalOutput): IO[ExitCode] = {
    def loop_inner(input: String): IO[ExitCode] = {
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
      _ <- IO(println(s"light is ${if (pin.isHigh) "Off" else "On"}"))
      input <- IO(StdIn.readLine())
      result <- loop_inner(input)
    } yield result
  }

  def program(gpio: GpioController): IO[ExitCode] = {
    (for {
      light <- Gpio.provision(gpio, RaspiPin.GPIO_25, "light", PinState.LOW)
      exit <- loop(light)
      _ <- Gpio.shutdown(gpio)
    } yield exit)
      .handleErrorWith(_ => {
        for {
          _ <- IO(gpio.shutdown())
          exit = ExitCode.Error
        } yield exit
      })
  }
}
