package com.machfour.macros.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

object DateTimeUtils {


    val ISO_LOCAL_HOUR_MINUTE: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .toFormatter()

    val LOCALIZED_DATETIME_MEDIUM : DateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault())

    // returns a ZonedDateTime from the given instant with the system default ZoneID
    fun Instant.toDateTime(): ZonedDateTime {
        return atZone(ZoneId.systemDefault())
    }

    fun Instant.asLocalHourMinute(is24HourFormat: Boolean = false): String {
        return ISO_LOCAL_HOUR_MINUTE.format(this.toDateTime())
    }

    fun Instant.toEpochSecond(truncate: ChronoUnit = ChronoUnit.SECONDS): Long {
        return truncatedTo(truncate).epochSecond
    }
}