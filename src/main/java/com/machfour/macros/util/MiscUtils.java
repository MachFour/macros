package com.machfour.macros.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MiscUtils {
    // Basically Objects.toString() except for Android's API level
    // Returns empty string on null input
    @NotNull
    public static String nullableToString(@Nullable Object o) {
        // TODO API level
        //return Objects.toString(o);
        return nullableToString(o, "");
    }
    @NotNull
    public static String nullableToString(@Nullable Object o, @NotNull String nullDefault) {
        // TODO API level
        //return Objects.toString(o);
        //return Objects.toString(o, nullDefault);
        return o == null ? nullDefault : o.toString();
    }

    // Basically Objects.equals() except for Android's API level
    public static boolean objectsEquals(@Nullable Object o1, @Nullable Object o2) {
        // TODO API level
        //return Objects.equals(o1, o2);
        return o1 == null ? o2 == null : o1.equals(o2);
    }


    // Copied from Integer.toUnsignedLong
    public static long toUnsignedLong(int x) {
        return (long)x & 4294967295L;
    }

    public static long toSignedLong(int x) {
        long unsignedValue = toUnsignedLong(x);
        return x >= 0 ? unsignedValue : -unsignedValue;
    }

    public static <E> List<E> toList(E e) {
        return Collections.singletonList(e);
    }
}
