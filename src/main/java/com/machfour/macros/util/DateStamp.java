package com.machfour.macros.util;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/*
 * Custom class for representing immutable Calendar dates without times.
 * When Java 8 is available, this class can be replaced with the native java.time classes.
 * e.g. LocalDate
 */
public class DateStamp implements Comparable<DateStamp> {
    // Used to format and parse ISO8601 date strings, e.g '2017-08-01'
    private static final DateTimeFormatter dateFormatter;
    // Fixed locale for machine-readable operations
    private static final Locale internalLocale = Locale.US;
    private static final TimeZone currentTimeZone = TimeZone.getDefault();
    //public static final Parcelable.Creator<DateStamp> CREATOR = new Creator();

    static {
        dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    }

    // this calendar instance's date and time
    public final int year;
    public final int month;
    public final int day;

    private final Clock internalClock = Clock.systemDefaultZone();
    private final LocalDate date;
    private final int hashCode;

    /*
     * Date stamp according to raw year, month, day fields
     */
    public DateStamp(int year, int month, int day) {
        this(LocalDate.of(year, month, day));
    }


    /*
     * Create date stamp from Calendar object representing
     * MIDNIGHT IN THE CURRENT TIME ZONE on some date
     */
    private DateStamp(@NotNull LocalDate date) {
        this.date = date;
        this.year = date.getYear();
        this.month = date.getMonthValue();
        this.day = date.getDayOfMonth();
        this.hashCode = makeHashCode(year, month, day);
    }

    /*
     * Returns a DateStamp representing a Calendar object's year, month and day.
     * The calendar will be converted into the current/default timezone first.
     */
    @NotNull
    public static DateStamp fromLocalDate(@NotNull LocalDate d) {
        return new DateStamp(d);
    }

    private static int makeHashCode(int year, int month, int day) {
        return Arrays.hashCode(new int[]{year, month, day});
    }

    /*
     * Return calendar corresponding to midnight on today's date in the current timezone
     */
    @NotNull
    private static LocalDate midnightToday() {
        return LocalDate.now();
    }

    // returns false for null
    public static boolean isInTheFuture(@NotNull DateStamp d) {
        return midnightToday().isBefore(d.date);
    }

    @NotNull
    public static String prettyPrint(@NotNull DateStamp day) {
        StringBuilder prettyStr = new StringBuilder(day.toString());
        DateStamp today = forCurrentDate();
        if (day.equals(today)) {
            prettyStr.append(" (today)");
        } else if (day.equals(today.step(-1))) {
            prettyStr.append(" (yesterday)");
        }
        return prettyStr.toString();
    }

    public LocalDate asMidnightOnDay() {
        return LocalDate.of(year, month, day);
    }

    public DateStamp step(int dayIncrement) {
        return new DateStamp(date.plusDays(dayIncrement));
    }

    /*
     * Get corresponding DateStamp for a number of days ago by using the Calendar's
     * field addition methods to add a negative amount of days
     */
    public static DateStamp forDaysAgo(int daysAgo) {
        return DateStamp.forCurrentDate().step(-1*daysAgo);
    }

    /*
     * Constructor for current moment
     */
    @NotNull
    public static DateStamp forCurrentDate() {
        return new DateStamp(midnightToday());
    }

    /*
     * Creates a new DateStamp instance from an ISO-8601 string.
     * Throws IllegalArgumentException if the string is in an invalid format
     */
    public static DateStamp fromIso8601String(@NotNull String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            return new DateStamp(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date string not in ISO-8601 format");
        }
    }

    /*
     * Converts this date into a number of days back from the today's date.
     * May return a negative number for dates in the future.
     */
    public static long daysSince(@NotNull DateStamp d) {
        return d.date.until(midnightToday(), ChronoUnit.DAYS);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DateStamp)) {
            return false;
        }
        DateStamp d = (DateStamp) o;
        return date.equals(d.date);
    }

    /*
     * Returns date in ISO-8601 format
     */
    @Override
    public String toString() {
        return date.toString();
    }

    @Override
    public int compareTo(@NotNull DateStamp other) {
        return this.date.compareTo(other.date);
    }

    // Number of days from this date since January 1, 1970.
    public long daysSince1Jan1970() {
        return date.toEpochDay();
    }

    public long toEpochMillis() {
        return ZonedDateTime.of(date, LocalTime.MIDNIGHT, ZoneId.systemDefault()).toEpochSecond()*1000L;
    }

}
