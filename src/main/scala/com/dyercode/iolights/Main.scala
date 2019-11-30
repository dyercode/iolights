package com.dyercode.iolights

import cats.effect.{ExitCode, IO, IOApp}
import com.dyercode.pi4jse.Gpio
import com.pi4j.io.gpio.{GpioPinDigitalOutput, PinState, RaspiPin}

import scala.annotation.tailrec
import scala.io.StdIn

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- IO(println("Enter to toggle. 'exit' to exit"))
      exit <- program
    } yield exit
  }

  def loop(pin: GpioPinDigitalOutput): IO[ExitCode] = {
    def loopInner(input: String): IO[ExitCode] = {
      if (input == "exit") {
        IO.pure(ExitCode.Success)
      } else {
        for {
          _ <- Gpio.toggle(pin)
          r <- loop(pin)
        } yield r
      }
    }

    for {
      _ <- IO(println(s"light is ${if (pin.isHigh) "Off" else "On"}"))
      input <- IO(StdIn.readLine())
      result <- loopInner(input)
    } yield result
  }

  def program: IO[ExitCode] = {
    (for {
      light <- Gpio.provisionOutput(RaspiPin.GPIO_25, "light", PinState.LOW)
      exit <- loop(light)
      _ <- Gpio.shutdown()
    } yield exit)
      .handleErrorWith { _ =>
        Gpio.shutdown().map(_ => ExitCode.Error)
      }
  }
}
