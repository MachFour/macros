package com.machfour.macros.util;

/*
 * Partial reimplementation of java.util.function.Function
 * (it's not available for Android API < 24)
 */
@FunctionalInterface
public interface Function<T, R> {
    R apply(T input);
}
