package com.machfour.macros.util

import kotlin.math.abs
import kotlin.math.roundToInt

// The Java-to-Kotlin conversion tool replaces Java's String.trim() with this code.
// We'll put it here for safekeeping.
fun String.javaTrim() : String = trim { it <= ' ' }

fun Double.toRoundedString(decimalPlaces: Int = 3, eps: Double = 1e-4): String {
    val asInt = roundToInt()
    val error = this - asInt
    return if (abs(error) < eps) {
        asInt.toString()
    } else {
        "%.${decimalPlaces}f".format(this)
    }
}


// copied from deprecated kotlin code
fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}