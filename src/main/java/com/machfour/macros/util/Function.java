package com.machfour.macros.util;

/*
 * Partial reimplementation of java.util.function.Function
 * (it's not available for Android API < 24)
 */
public interface Function<T, R> {
    R apply(T input);
}
