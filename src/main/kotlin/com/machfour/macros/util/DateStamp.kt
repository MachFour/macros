package com.machfour.macros.util

import java.time.*
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/*
 * Custom class for representing immutable Calendar dates without times.
 * It's basically a wrapper around java.time.LocalDate
 * When Java 8 is available on android (which is now), this class can be replaced with the
 * native java.time classes
 */
open class DateStamp private constructor(val date: LocalDate) : Comparable<DateStamp> {

    /*
     * Create date stamp from Calendar object representing
     * MIDNIGHT IN THE CURRENT TIME ZONE on some date
     */
    val year: Int = date.year
    val month: Int = date.monthValue
    val day: Int = date.dayOfMonth

    private val internalClock = Clock.systemDefaultZone()
    private val hashCode: Int = makeHashCode(year, month, day)

    /*
     * Date stamp according to raw year, month, day fields
     */
    constructor(year: Int, month: Int, day: Int) : this(LocalDate.of(year, month, day))

    fun step(dayIncrement: Long): DateStamp = DateStamp(date.plusDays(dayIncrement.toLong()))

    override fun hashCode(): Int = hashCode

    override fun equals(other: Any?): Boolean = (other is DateStamp) && date == other.date

    /*
     * Returns date in ISO-8601 format
     */
    override fun toString(): String = date.toString()

    override fun compareTo(other: DateStamp): Int = date.compareTo(other.date)

    fun isInTheFuture(): Boolean = date.isAfter(LocalDate.now())

    // Number of days from this date since January 1, 1970.
    fun daysSince1Jan1970(): Long = date.toEpochDay()

    fun toEpochMillis(): Long {
        return ZonedDateTime.of(date, LocalTime.MIDNIGHT, ZoneId.systemDefault()).toEpochSecond() * 1000L
    }

    companion object {
        /*
         * Returns a DateStamp representing a Calendar object's year, month and day.
         * The calendar will be converted into the current/default timezone first.
         */
        fun ofLocalDate(d: LocalDate): DateStamp = DateStamp(d)

        private fun makeHashCode(year: Int, month: Int, day: Int): Int {
            return intArrayOf(year, month, day).contentHashCode()
        }

        fun prettyPrint(day: DateStamp): String {
            val prettyStr = StringBuilder(day.toString())
            val today = currentDate()
            if (day == today) {
                prettyStr.append(" (today)")
            } else if (day == today.step(-1)) {
                prettyStr.append(" (yesterday)")
            }
            return prettyStr.toString()
        }

        /*
         * Get corresponding DateStamp for a number of days ago by using the Calendar's
         * field addition methods to add a negative amount of days
         */
        fun forDaysAgo(daysAgo: Long): DateStamp = currentDate().step(-1 * daysAgo)

        /*
         * Constructor for current moment
         */
        fun currentDate(): DateStamp = DateStamp(LocalDate.now())

        /*
         * Creates a new DateStamp instance from an ISO-8601 string, e.g '2017-08-01'
         * Throws IllegalArgumentException if the string is in an invalid format
         */
        fun fromIso8601String(dateString: String): DateStamp {
            return try {
                val date = LocalDate.parse(dateString)
                DateStamp(date)
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("Date string not in ISO-8601 format")
            }
        }

        /*
         * Converts this date into a number of days back from the today's date.
         * May return a negative number for dates in the future.
         */
        fun daysSince(d: DateStamp): Long = d.date.until(LocalDate.now(), ChronoUnit.DAYS)
    }

}