package com.dyercode.iolights

import cats.effect.IO
import com.pi4j.io.gpio._

object Gpio {
  def initialize(): IO[GpioController] = IO(GpioFactory.getInstance())

  def provision(
      gpio: GpioController,
      pin: Pin,
      name: String,
      state: PinState
  ): IO[GpioPinDigitalOutput] = {
    IO(gpio.provisionDigitalOutputPin(pin, name, state))
  }

  def shutdown(gpio: GpioController): IO[Unit] = {
    IO(gpio.shutdown())
  }
}
