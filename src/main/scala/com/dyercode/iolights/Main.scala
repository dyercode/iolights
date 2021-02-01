package com.dyercode.iolights

import cats.effect.{ExitCode, IO, IOApp}
import com.dyercode.iolights.LightStatus.{Off, On}
import com.dyercode.pi4jsw.Gpio
import com.pi4j.io.gpio.{GpioPinDigitalOutput, PinState, RaspiPin}

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
          _ <- IO(pin.toggle())
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
      gpio <- Gpio.initialize()
      light <- Gpio.provision(gpio, RaspiPin.GPIO_25, "light", PinState.LOW)
      fiber <- Schedule
        .loop(
          None,
          None,
          {
            case On  => IO({ println("ouch ON"); light.low() })
            case Off => IO({ println("ouch OFF"); light.high() })
          }
        )
        .start
      exit <- loop(light)
      _ <- Gpio.shutdown(gpio)
    } yield exit)
      .handleErrorWith { _ =>
        Gpio.shutdown().map(_ => ExitCode.Error)
      }
  }
}
