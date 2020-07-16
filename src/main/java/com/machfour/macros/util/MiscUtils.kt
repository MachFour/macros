package com.machfour.macros.util

object MiscUtils {
    // Copied from Integer.toUnsignedLong
    @JvmStatic
    fun toUnsignedLong(x: Int): Long {
        return x.toLong() and 4294967295L
    }

    @JvmStatic
    fun toSignedLong(x: Int): Long {
        val unsignedValue = toUnsignedLong(x)
        return if (x >= 0) unsignedValue else -unsignedValue
    }

    @JvmStatic
    fun <E> toList(e: E): List<E> {
        return listOf(e)
    }

    /*
     * This is the version of trim that the Java-to-Kotlin conversion tool
     * replaces Java's String.trim() with. We'll put it here for safe keeping
     */
    fun String.javaTrim() : String {
        return trim { it <= ' ' }
    }
}