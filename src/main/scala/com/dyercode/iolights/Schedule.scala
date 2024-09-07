package com.dyercode.iolights

import cats.effect.*
import cats.implicits.*
import cats.{Foldable, Monoid, Semigroup, Traverse, TraverseFilter}
import com.github.tototoshi.csv.*

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalTime, ZoneId}
import scala.concurrent.duration.*
import scala.util.{Failure, Success, Try}

sealed trait LightStatus

object LightStatus {
  def on: LightStatus = On
  def off: LightStatus = Off
  case object Off extends LightStatus
  case object On extends LightStatus

  def apply(string: String): Option[LightStatus] = string.toLowerCase match {
    case "on"  => Some(LightStatus.On)
    case "off" => Some(LightStatus.Off)
    case _     => None
  }

  def unapply(ls: LightStatus): String = ls match {
    case LightStatus.On  => "on"
    case LightStatus.Off => "off"
  }

  implicit val lightStatusLastWins: Semigroup[LightStatus] =
    (_: LightStatus, b: LightStatus) => b
}

case class Schedule(config: Conf) {
  def loop(trigger: LightStatus => IO[?]): IO[?] = loop(None, None, trigger)

  def loop(
      a: Option[LocalTime],
      b: Option[LocalTime],
      trigger: LightStatus => IO[?],
  ): IO[Unit] = {
    for {
      aa <- Schedule.now[IO]
      bb <- Schedule.now[IO]
      _ <- loop(a.getOrElse(aa), b.getOrElse(bb), trigger)
    } yield ()
  }

  def loop(
      a: LocalTime,
      b: LocalTime,
      trigger: LightStatus => IO[?],
  ): IO[Unit] = {
    for {
      s <- load
      scheduledChange <- IO(checkAllSchedules(a, b, s))
      _ <- (scheduledChange: Option[LightStatus]) match {
        case Some(a) => trigger(a)
        case _       => IO.unit
      }
      _ <- IO.sleep(30.seconds)
      t <- Schedule.now[IO]
      _ <- loop(b, t, trigger)
    } yield ()
  }

  private def checkAllSchedules(
      as: LocalTime,
      bs: LocalTime,
      ss: Map[LocalTime, LightStatus],
  ): Option[LightStatus] = Monoid.combineAll[Option[LightStatus]](
    ss.map(checkScheduleItemTriggered(_, as, bs))
  )

  def checkScheduleItemTriggered(
      scheduleEntry: (LocalTime, LightStatus),
      before: LocalTime,
      after: LocalTime,
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
      end: LocalTime,
  ): Boolean = {
    (begin.isBefore(x) || begin == x) && (end.isAfter(x) || end == x)
  }

  def load: IO[Map[LocalTime, LightStatus]] = for {
    ss <- read(config.scheduleFile)
    res <- IO(parseSchedule(ss))
  } yield res

  def parseSchedule[F[_]: Foldable](
      ss: F[List[String]]
  ): Map[LocalTime, LightStatus] =
    Foldable[F].foldLeft(ss, Map[LocalTime, LightStatus]()) { (acc, value) =>
      (value match {
        case List(ts, lss) =>
          for {
            ls <- LightStatus(lss)
            t <- Schedule.parseTime(ts).toOption
          } yield (t, ls)
        case _ => None
      }) match {
        case Some((t, ls)) => acc + (t -> ls)
        case None          => acc
      }
    }

  private def read(filename: String): IO[List[List[String]]] =
    Resource.fromAutoCloseable(IO(CSVReader.open(filename))).use { reader =>
      IO(reader.all())
    }
}

object Schedule {
  private val clockFormat = DateTimeFormatter.ofPattern("H:mm")

  private def instantToTime[F[_]: Sync](instant: Instant): F[LocalTime] =
    Sync[F].delay(
      instant.atZone(ZoneId.systemDefault()).toLocalTime
    )

  def now[F[_]: Sync]: F[LocalTime] = for {
    instant <- Sync[F].realTimeInstant
    time <- instantToTime[F](instant)
  } yield time

  def parseTime(str: String): Try[LocalTime] = Try(
    LocalTime.parse(str, clockFormat)
  )
}

type Entry = (LocalTime, LightStatus)
