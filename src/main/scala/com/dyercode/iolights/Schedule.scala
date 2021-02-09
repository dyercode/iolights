package com.dyercode.iolights

import cats.effect.{Clock, Concurrent, Sync, Timer}
import cats.implicits._
import cats.{Monoid, Semigroup}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalTime, ZoneId}
import scala.concurrent.duration
import scala.concurrent.duration._
import scala.util.{Success, Try}

sealed trait LightStatus
object LightStatus {
  def on: LightStatus = On
  def off: LightStatus = Off
  case object Off extends LightStatus
  case object On extends LightStatus
  implicit val lightStatusLastWins: Semigroup[LightStatus] =
    (_: LightStatus, b: LightStatus) => b
}

object Schedule {
  def loop[F[_]: Concurrent](
      a: Option[LocalTime],
      b: Option[LocalTime],
      trigger: LightStatus => F[_]
  )(implicit timer: Timer[F]): F[_] = {
    for {
      aa <- now[F]
      bb <- now[F]
      _ <- loop(a.getOrElse(aa), b.getOrElse(bb), trigger)
    } yield ()
  }

  def loop[F[_]: Sync](
      a: LocalTime,
      b: LocalTime,
      trigger: LightStatus => F[_]
  )(implicit timer: Timer[F]): F[_] = {
    for {
      scheduledChange <- Sync[F].delay(checkAllSchedules(a, b))
      _ <- scheduledChange match {
        case Some(a) => trigger(a)
        case _       => Sync[F].unit
      }
      _ <- timer.sleep(30.seconds)
      t <- now[F]
      _ <- loop(b, t, trigger)
    } yield ()
  }

  private def checkAllSchedules(
      as: LocalTime,
      bs: LocalTime
  ): Option[LightStatus] = {
    Monoid.combineAll[Option[LightStatus]](
      schedule(scheduleHumanReadable).map(checkScheduleItemTriggered(_, as, bs))
    )
  }

  def parseTime(str: String): Try[LocalTime] = {
    Try(LocalTime.parse(str, clockFormat))
  }

  private def epochToTime[F[_]: Sync](epoch: F[Long]): F[LocalTime] = {
    for {
      e <- epoch
      time <- Sync[F].delay(
        Instant.ofEpochMilli(e).atZone(ZoneId.systemDefault()).toLocalTime
      )
    } yield time
  }

  def now[F[_]: Sync](implicit clock: Clock[F]): F[LocalTime] = {
    for {
      epoch <- Sync[F].delay(clock.realTime(duration.MILLISECONDS))
      time <- epochToTime(epoch)
    } yield time
  }

  def checkScheduleItemTriggered(
      scheduleEntry: (LocalTime, LightStatus),
      before: LocalTime,
      after: LocalTime
  ): Option[LightStatus] = {
    val (triggerTime, newStatus) = scheduleEntry

    if (before.isAfter(after)) {
      checkScheduleItemTriggered(scheduleEntry, before, LocalTime.MAX) |+|
        checkScheduleItemTriggered(scheduleEntry, LocalTime.MIDNIGHT, after)
    } else if (between(triggerTime, before, after)) {
      Some(newStatus)
    } else {
      None
    }
  }

  private def between(
      x: LocalTime,
      begin: LocalTime,
      end: LocalTime
  ): Boolean = {
    (begin.isBefore(x) || begin == x) && (end.isAfter(x) || end == x)
  }

  private val clockFormat = DateTimeFormatter.ofPattern("H:mm")
  val scheduleHumanReadable: Map[String, LightStatus] =
    Map[String, LightStatus](
      "3:20" -> LightStatus.On,
      "6:20" -> LightStatus.Off
    )

  def schedule(
      humanReadable: Map[String, LightStatus]
  ): Map[LocalTime, LightStatus] = {
    humanReadable
      .map { case (key, value) => (parseTime(key), value) }
      .collect { case (Success(d), v) => (d, v) }
  }
}
