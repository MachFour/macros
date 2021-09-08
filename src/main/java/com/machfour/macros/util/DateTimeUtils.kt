package com.machfour.macros.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

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

val LOCALIZED_DATETIME_MEDIUM: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDateTime(FormatStyle.MEDIUM)
    .withZone(ZoneId.systemDefault())

val LOCALIZED_DATETIME_SHORT : DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDateTime(FormatStyle.SHORT)
    .withZone(ZoneId.systemDefault())

// returns a ZonedDateTime from the given instant with the system default ZoneID
fun Instant.toDateTime(): ZonedDateTime {
    return atZone(ZoneId.systemDefault())
}

fun Instant.asLocalHourMinute(is24HourFormat: Boolean = false): String {
    val fmt = if (is24HourFormat) ISO_LOCAL_HOUR_MINUTE else ISO_LOCAL_HOUR_MINUTE_12H
    return fmt.format(this.toDateTime())
}

fun Instant.toEpochSecond(truncate: ChronoUnit = ChronoUnit.SECONDS): Long {
    return truncatedTo(truncate).epochSecond
}