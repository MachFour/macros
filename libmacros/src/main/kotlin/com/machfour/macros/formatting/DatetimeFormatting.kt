package com.machfour.macros.formatting

import com.machfour.datestamp.DateStamp
import com.machfour.datestamp.makeDateStamp
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Meal
import kotlinx.datetime.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import kotlin.time.Duration.Companion.nanoseconds

val MacrosEntity<*>.createInstant: Instant
    get() = Instant.fromEpochSeconds(createTime)

val MacrosEntity<*>.modifyInstant: Instant
    get() = Instant.fromEpochSeconds(modifyTime)

val Meal.startTimeInstant: Instant
    get() = Instant.fromEpochSeconds(startTime)


val ISO_LOCAL_HOUR_MINUTE_12H: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.CLOCK_HOUR_OF_AMPM, 2)
    .appendLiteral(':')
    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
    .appendLiteral(' ')
    .appendText(ChronoField.AMPM_OF_DAY)
    .toFormatter()
val ISO_LOCAL_HOUR_MINUTE: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.HOUR_OF_DAY, 2)
    .appendLiteral(':')
    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
    .toFormatter()
val FILE_TIMESTAMP: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 4)
    .appendLiteral('-')
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral('-')
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .appendLiteral('T')
    .appendValue(ChronoField.HOUR_OF_DAY, 2)
    .appendLiteral(':')
    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
    .appendLiteral(':')
    .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
    .toFormatter()
val LOCALIZED_DATE_MEDIUM: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
val LOCALIZED_DATE_SHORT: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
val LOCALIZED_DATETIME_MEDIUM: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    .withZone(ZoneId.systemDefault())
val LOCALIZED_DATETIME_SHORT: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    .withZone(ZoneId.systemDefault())

// returns a ZonedDateTime from the given instant with the system default ZoneID
fun Instant.toLocalDateTimeWithDefaultTz(): LocalDateTime {
    return toLocalDateTime(TimeZone.currentSystemDefault())
}

fun Instant.asLocalHourMinute(is24HourFormat: Boolean = false): String {
    val fmt = if (is24HourFormat) ISO_LOCAL_HOUR_MINUTE else ISO_LOCAL_HOUR_MINUTE_12H
    return fmt.format(this.toLocalDateTimeWithDefaultTz())
}

fun DateTimeFormatter.format(dateTime: LocalDateTime): String {
    return format(dateTime.toJavaLocalDateTime())
}

fun Instant.truncateToSecond(): Instant {
    return minus(nanosecondsOfSecond.nanoseconds)
}

fun currentTimeString(): String {
    return FILE_TIMESTAMP.format(Clock.System.now().toLocalDateTimeWithDefaultTz())
}

fun LocalDate.toDateStamp(): DateStamp {
    return makeDateStamp(year, monthNumber, dayOfMonth)
}

fun DateStamp.toLocalDate(): LocalDate {
    return LocalDate(year, month, day)
}
