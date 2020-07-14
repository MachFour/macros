package com.machfour.macros.util

import java.util.function.Function

/*
 * Class to do string joining of arbitrary iterable objects, using a custom string function
 * Similar functionality exists as java.util.StringJoiner but not all of this functionality is
 * available in Android
 * Has a builder interface with default parameters:
 * separator is " "
 * suffix after each item is ""
 * string function is Object::toString
 */
class StringJoiner<E> private constructor(private val iterator: Iterator<E>) {
    private var sep = ""
    private var suffix = ""
    private var stringFunc: (E) -> String = { it.toString() }

    // how many copies of each element
    private var copies = 1

    fun sep(sep: String): StringJoiner<E> {
        this.sep = sep
        return this
    }

    fun suffix(suffix: String): StringJoiner<E> {
        this.suffix = suffix
        return this
    }

    fun copies(copies: Int): StringJoiner<E> {
        require(copies >= 1) { "copies must be >= 1" }
        this.copies = copies
        return this
    }

    fun stringFunc(stringFunc: Function<E, String>): StringJoiner<E> {
        this.stringFunc = { stringFunc.apply(it) }
        return this
    }

    // StringFunc is arbitary function to apply to object to produce a string
    fun join(): String {
        if (!iterator.hasNext()) {
            return ""
        }
        val joined = StringBuilder()
        // there will be one last separator string at the end but we'll remove it
        while (iterator.hasNext()) {
            val next = stringFunc(iterator.next())
            for (i in 1..copies) {
                joined.append(next)
                joined.append(suffix)
                joined.append(sep)
            }
        }
        // remove the last sep
        if (sep.isNotEmpty()) {
            joined.delete(joined.length - sep.length, joined.length - 1)
        }
        return joined.toString()
    }

    companion object {
        @JvmStatic
        fun <E> of(iterator: Iterator<E>): StringJoiner<E> {
            return StringJoiner(iterator)
        }

        @JvmStatic
        fun <E> of(iterable: Iterable<E>): StringJoiner<E> {
            return StringJoiner(iterable.iterator())
        }

        @JvmStatic
        fun <E> of(element: E): StringJoiner<E> {
            return StringJoiner(listOf(element).iterator())
        }
    }

}