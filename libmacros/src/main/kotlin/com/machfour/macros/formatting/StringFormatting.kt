package com.machfour.macros.formatting

import com.machfour.macros.formatting.fprounding.format
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

fun CharSequence.fmt(minWidth: Int, alignLeft: Boolean = false, padChar: Char = ' '): String {
    val padding = padChar.toString().repeat(max(0, minWidth - length))
    return toString().let { if (alignLeft) it + padding else padding + it }
}

fun Float.toString(precision: Int, withTrailingZeros: Boolean = true): String {
    return toDouble().toString(precision, withTrailingZeros)
}

fun Double.toString(precision: Int, withTrailingZeros: Boolean = true): String {
    if (precision < 0 || isNaN() || isInfinite()) {
        return toString()
    }

    val s = format(precision)
    val dpIndex = s.indexOf('.')
    val hasDp = dpIndex != -1

    if (withTrailingZeros && precision > 0) {
        // add more zeros if current precision is not enough,
        // and add decimal point if there isn't one.
        val suffix = if (hasDp) {
            // extra precision = max(precision - current precision, 0)
            "0".repeat(max(precision - (s.lastIndex - dpIndex), 0))
        } else {
            "." + "0".repeat(precision)
        }
        return s + suffix
    }

    if (!withTrailingZeros && hasDp && s.endsWith('0')) {
        // return slice without trailing zeros
        var lastIncludedIndex = s.indexOfLast { it != '0' }
        if (s[lastIncludedIndex] == '.') {
            lastIncludedIndex--
        }
        return s.substring(0, lastIncludedIndex + 1)
    }

    return s
}

// Returns this double as a string, rounded to the nearest integer if it is within roundTolerance,
// otherwise as a decimal point number with the given precision.
// TODO unit test
fun Double.toStringWithRounding(maxPrecision: Int = 3, roundTolerance: Double = 1e-4): String {
    val asInt = roundToLong()
    val error = this - asInt
    return if (abs(error) < roundTolerance) {
        asInt.toString()
    } else {
        toString(maxPrecision, withTrailingZeros = false)
    }
}
