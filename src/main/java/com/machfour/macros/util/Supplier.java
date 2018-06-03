package com.machfour.macros.util;

/*
 * Reimplementation of java.util.function.Supplier
 * (it's not available for Android API < 24)
 */
public interface Supplier<T> {
    T get();
}
