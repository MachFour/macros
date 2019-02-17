package com.machfour.macros.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
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
    // how many copies of each element
    private int copies;

    private final Iterator<E> iterator;

    public static <E> StringJoiner<E> of(Iterator<E> iterator) {
        return new StringJoiner<>(iterator);
    }
    public static <E> StringJoiner<E> of(Iterable<E> iterable) {
        return new StringJoiner<>(iterable);
    }
    public static <E> StringJoiner<E> of(E element) {
        return new StringJoiner<>(element);
    }

    private StringJoiner(Iterator<E> iterator) {
        this.iterator = iterator;
        this.sep = "";
        this.suffix = "";
        this.copies = 1;
        this.stringFunc = Object::toString;
    }
    private StringJoiner(Iterable<E> iterable) {
        this(iterable.iterator());
    }
    private StringJoiner(E element) {
        this(Collections.singletonList(element));
    }

    public StringJoiner<E> sep(@NotNull String sep) {
        this.sep = sep;
        return this;
    }
    public StringJoiner<E> suffix(@NotNull String suffix) {
        this.suffix = suffix;
        return this;
    }
    public StringJoiner<E> copies(int copies) {
        if (copies < 1) {
            throw new IllegalArgumentException("copies must be >= 1");
        }
        this.copies = copies;
        return this;
    }

    public StringJoiner<E> stringFunc(@NotNull Function<E, String> stringFunc) {
        this.stringFunc = stringFunc;
        return this;
    }

    // StringFunc is arbitary function to apply to object to produce a string
    @NotNull
    public String join() {
        if (!iterator.hasNext()) {
            return "";
        }
        StringBuilder joined = new StringBuilder();
        // there will be one last separator string at the end but we'll remove it
        while (iterator.hasNext()) {
            String next = stringFunc.apply(iterator.next());
            for (int i = 1; i <= copies; ++i) {
                joined.append(next);
                joined.append(suffix);
                joined.append(sep);
            }
        }
        // remove the last sep
        if (!sep.isEmpty()) {
            joined.delete(joined.length() - sep.length(), joined.length() - 1);
        }
        return joined.toString();
    }
}
