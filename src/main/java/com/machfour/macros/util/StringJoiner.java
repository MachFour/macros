package com.machfour.macros.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/*
 * Class to do string joining of arbitrary iterable objects, using a custom string function
 * Similar functionality exists as java.util.StringJoiner but not all of this functionality is
 * available in Android
 * Has a builder interface with default parameters:
 * separator is " "
 * suffix after each item is ""
 * string function is Object::toString
 */
public class StringJoiner<E> {
    @NotNull
    private String sep;
    @NotNull
    private String suffix;
    private Function<E, String> stringFunc;

    private final Iterator<E> iterator;

    public StringJoiner(Iterator<E> iterator) {
        this.iterator = iterator;
        this.sep = " ";
        this.suffix = "";
        this.stringFunc = Object::toString;
    }
    public StringJoiner(Iterable<E> iterable) {
        this(iterable.iterator());
    }

    public StringJoiner<E> sep(@NotNull String sep) {
        this.sep = sep;
        return this;
    }
    public StringJoiner<E> suffix(@NotNull String suffix) {
        this.suffix = suffix;
        return this;
    }
    public StringJoiner<E> stringFunc(@NotNull Function<E, String> stringFunc) {
        this.stringFunc = stringFunc;
        return this;
    }

    // StringFunc is arbitary function to apply to object to produce a string
    public String join() {
        StringBuilder joined = new StringBuilder();
        if (iterator.hasNext()) {
            joined.append(stringFunc.apply(iterator.next()));
            joined.append(suffix);
            while (iterator.hasNext()) {
                joined.append(sep);
                joined.append(stringFunc.apply(iterator.next()));
                joined.append(suffix);
            }
        }
        return joined.toString();
    }
}
