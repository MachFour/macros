package com.machfour.macros.util;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private static final DateFormat dateFormatter;
    // Fixed locale for machine-readable operations
    private static final Locale internalLocale = Locale.US;
    private static final TimeZone currentTimeZone = TimeZone.getDefault();
    //public static final Parcelable.Creator<DateStamp> CREATOR = new Creator();

    static {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", internalLocale);
        dateFormatter.setTimeZone(currentTimeZone);
    }

    // this calendar instance's date and time
    public final int year;
    public final int month;
    public final int day;
    
    private final Calendar midnightOnDay;
    private final int hashCode;

    /*
     * Date stamp according to raw year, month, day fields
     */
    public DateStamp(int year, int month, int day) {
        this(midnightOnDate(year, month, day));
    }


    /*
     * Create date stamp from Calendar object representing
     * MIDNIGHT IN THE CURRENT TIME ZONE on some date
     */
    private DateStamp(@NotNull Calendar midnight) {
        this.midnightOnDay = midnight;
        this.year = midnight.get(Calendar.YEAR);
        this.month = midnight.get(Calendar.MONTH);
        this.day = midnight.get(Calendar.DAY_OF_MONTH);
        this.hashCode = makeHashCode(year, month, day);
    }

    /*
     * Returns a DateStamp representing a Calendar object's year, month and day.
     * The calendar will be converted into the current/default timezone first.
     */
    @NotNull
    public static DateStamp fromCalendar(@NotNull Calendar c) {
        c.setTimeZone(currentTimeZone);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return new DateStamp(c);
    }

    /*
     * Return calendar corresponding to midnight in the current timezone
     * on the given day
     */
    @NotNull
    private static Calendar midnightOnDate(int year, int month, int day) {
        Calendar c = midnightToday();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        return c;
    }

    private static int makeHashCode(int year, int month, int day) {
        return Arrays.hashCode(new int[]{year, month, day});
    }

    /*
     * Return calendar corresponding to midnight on today's date in the current timezone
     */
    @NotNull
    private static Calendar midnightToday() {
        Calendar c = Calendar.getInstance(currentTimeZone, internalLocale);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    // returns false for null
    public static boolean isInTheFuture(DateStamp d) {
        return (d != null) && (midnightToday().compareTo(d.midnightOnDay) < 0);
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

    public Calendar asMidnightOnDay() {
        return (Calendar) midnightOnDay.clone();
    }

    public DateStamp step(int dayIncrement) {
        Calendar then = midnightOnDate(year, month, day);
        then.add(Calendar.DATE, dayIncrement);
        return new DateStamp(then);
    }

    /*
     * Get corresponding DateStamp for a number of days ago by using the Calendar's
     * field addition methods to add a negative amount of days
     */
    public static DateStamp forDaysAgo(int daysAgo) {
        Calendar then = midnightToday();
        then.add(Calendar.DATE, -1 * daysAgo);
        return new DateStamp(then);
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
        Calendar date = Calendar.getInstance();
        try {
            date.setTime(dateFormatter.parse(dateString));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date string not in ISO-8601 format");
        }
        return new DateStamp(date);
    }

    /*
     * Converts this date into a number of days back from the today's date.
     * May return a negative number for dates in the future.
     */
    public static long daysSince(DateStamp d) {
        if (d == null) {
            return 0;
        }
        long millisDifference = midnightToday().getTimeInMillis()
            - d.midnightOnDay.getTimeInMillis();
        return TimeUnit.DAYS.convert(millisDifference, TimeUnit.MILLISECONDS);
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
        return year == d.year && month == d.month && day == d.day;
    }

    /*
     * Returns date in ISO-8601 format
     */
    @Override
    public String toString() {
        return dateFormatter.format(midnightOnDay.getTime());
    }

    @Override
    public int compareTo(@NotNull DateStamp other) {
        return midnightOnDay.compareTo(other.midnightOnDay);
    }

    // Number of days from this date since January 1, 1970.
    public long daysSince1Jan1970() {
        long unixTimeMillis = midnightOnDay.getTimeInMillis();
        return TimeUnit.DAYS.convert(unixTimeMillis, TimeUnit.MILLISECONDS);
    }

}
