package com.machfour.macros.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.ChronoField

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
    fun instantToDateTime(i: Instant): ZonedDateTime {
        return i.atZone(ZoneId.systemDefault())
    }

}