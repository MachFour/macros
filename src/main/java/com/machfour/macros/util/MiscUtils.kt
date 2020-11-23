package com.machfour.macros.util

object MiscUtils {
    // The Java-to-Kotlin conversion tool replaces Java's String.trim() with this code.
    // We'll put it here for safekeeping.
    fun String.javaTrim() : String = trim { it <= ' ' }

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