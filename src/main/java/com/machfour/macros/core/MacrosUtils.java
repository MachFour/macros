package com.machfour.macros.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MacrosUtils {

    private MacrosUtils() {
    }

    public static void exceptionIfNull(Object variable, String name) {
        if (variable == null) {
            String error_msg = "'" + name + "' cannot be null";
            throw new IllegalArgumentException(error_msg);
        }
    }

    public static void exceptionIfBothNonNull(Object a, Object b, String sA, String sB) {
        if (a != null && b != null) {
            String error_msg = "'" + sA + "' and '" + sB + "' cannot both be non-null";
            throw new IllegalArgumentException(error_msg);
        }
    }

    public static void exceptionIfNotEqual(Object a, Object b, String nameA, String nameB) {
        if ((a != null && !a.equals(b)) || a == null && b != null) {
            String error_msg = nameA + " and " + nameB + " must be equal";
            throw new IllegalArgumentException(error_msg);
        }
    }

    public static void exceptionIfNotPositive(long variable, String name) {
        if (variable <= 0) {
            String error_msg = "'" + name + "' must be positive";
            throw new IllegalArgumentException(error_msg);
        }
    }

    public static void exceptionIfNotPositive(double variable, String name) {
        if (variable <= 0) {
            String error_msg = "'" + name + "' must be positive";
            throw new IllegalArgumentException(error_msg);
        }
    }

    public static <K, V> Map<K, Boolean> makeHasDataMap(Map<K, V> data) {
        Map<K, Boolean> hasData = new HashMap<>();
        for (K key : data.keySet()) {
            hasData.put(key, data.get(key) != null);
        }
        return hasData;
    }

    /*
     * Date.getTime() and Date constructor both deal with time in milliseconds,
     * whereas unix time is in seconds. Both are measured from 00:00 UTC on Jan 1, 1970.
     */
    public static Date dateFromUnixTime(long unixTime) {
        return new Date(unixTime * 1000L);
    }

    public static long currentUnixTime() {
        return unixTimeFromDate(new Date());
    }

    public static long unixTimeFromDate(Date date) {
        return date.getTime() / 1000L;
    }

    public static long idFromString(String value) {
        return "".equals(value) ? MacrosPersistable.NO_ID : Long.valueOf(value);
    }

    public static long getCurrentTimeStamp() {
        // Date.getTime() return millis, we want seconds
        return new Date().getTime() / 1000L;
    }

    public static String idToString(long id) {
        return id == MacrosPersistable.NO_ID ? "" : Long.toString(id);
    }

    public static long unboxNullableID(Long nullableId) {
        return nullableId == null ? MacrosPersistable.NO_ID : nullableId;
    }

    public static boolean unboxNullableBoolean(Boolean nullableBoolean) {
        return nullableBoolean == null ? false : nullableBoolean;
    }

    public static Boolean boxNullableBoolean(boolean b) {
        return b ? true : null;
    }

    public static String nullableString(String value) {
        return "".equals(value) ? null : value;
    }

    public static Double nullableDoubleFromString(String value) {
        return "".equals(value) ? null : Double.valueOf(value);
    }

    public static double doubleFromString(String value) {
        return Double.valueOf(value);
    }

    public static String makeStringValue(Object value) {
        if (value instanceof Double) {
            return String.format(Locale.getDefault(), "%.1f", (Double) value);
        } else if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }
}