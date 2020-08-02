package com.machfour.macros.util

object MiscUtils {
    /*
     * This is the version of trim that the Java-to-Kotlin conversion tool
     * replaces Java's String.trim() with. We'll put it here for safe keeping
     */
    fun String.javaTrim() : String = trim { it <= ' ' }

    fun <T> nCopies(number: Int, obj: T) : List<T> {
        return ArrayList<T>(number).apply { repeat(number) { add (obj) } }
    }

    // from Java Math library
    fun Int.floorDiv(y: Int): Int {
        var r = this / y
        // if the signs are different and modulo not zero, round down
        if (this xor y < 0 && r * y != this) {
            r--
        }
        return r
    }

    // from Java Math library
    fun Int.floorMod(y: Int): Int {
        return this - this.floorDiv(y) * y
    }
}