package com.machfour.macros.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

// Native Kotlin string and floating point formatting
fun CharSequence.fmt(minWidth: Int, leftAlign: Boolean = false, padChar: Char = ' '): String {
    val numChars = (minWidth - length).coerceAtLeast(0)
    val s = this
    return buildString {
        append(padChar.toString().repeat(numChars))
        if (leftAlign) insert(0, s) else append(s)
    }
}

fun Float.toString(precision: Int): String {
    return toDouble().toString(precision)
}

fun Double.toString(precision: Int, trimTrailingZeros: Boolean = false): String {
    require(precision >= 0) { "Precision must be >= 0 (was $precision)" }
    return when {
        isNaN() -> "NaN"
        isInfinite() -> if (this < 0) "-∞" else "∞"
        this == 0.0 -> if (precision == 0 || trimTrailingZeros) "0" else "0." + "0".repeat(precision)
        else -> {
            val multiplied = this * 10.0.pow(precision)
            // Check if operation is out of range; if so denote it with a > or < sign as appropriate
            val s = multiplied.roundToLong().toString()

            buildString {
                if (multiplied > Long.MAX_VALUE) {
                    // roundToLong() will truncate to Long.MAX_VALUE
                    append('>')
                } else if (multiplied < Long.MIN_VALUE) {
                    // roundToLong() will truncate to Long.MIN_VALUE
                    append('<')
                }
                if (precision > 0) {
                    // put in decimal place in the right spot
                    if (s.length - precision == 0) {
                        append('0')
                    } else {
                        append(s.substring(0, s.length - precision))
                    }
                    append('.')
                    append(s.substring(s.length - precision, s.length))

                    if (trimTrailingZeros && endsWith('0')) {
                        var trailingZerosStart = lastIndex

                        while(trailingZerosStart > 0 && this[trailingZerosStart - 1] == '0') {
                            trailingZerosStart--
                        }

                        deleteRange(trailingZerosStart, length)

                        // remove decimal point if needed
                        if (endsWith('.')) {
                            deleteCharAt(lastIndex)
                        }
                    }
                } else {
                    append(s)
                }
            }
        }
    }
}

fun Double.toRoundedString(maxPrecision: Int = 3, roundTolerance: Double = 1e-4): String {
    val asInt = roundToLong()
    val error = this - asInt
    return if (abs(error) < roundTolerance) {
        asInt.toString()
    } else {
        toString(maxPrecision, trimTrailingZeros = true)
    }
}
