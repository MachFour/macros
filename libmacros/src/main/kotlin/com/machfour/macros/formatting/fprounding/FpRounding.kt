package com.machfour.macros.formatting.fprounding

// FpRounding implements JVM-specific methods to round floating point
// numbers to a given precision

fun Double.format(precision: Int): String {
    if (precision < 0 || isNaN() || isInfinite()) {
        return toString()
    }

    return "%.${precision}f".format(this)
}

fun Float.round(precision: Int): Float {
    if (precision < 0 || isNaN() || isInfinite()) {
        return this
    }
    return toDouble().format(precision).toFloat()
}

fun Double.round(precision: Int): Double {
    if (precision < 0 || isNaN() || isInfinite()) {
        return this
    }
    return format(precision).toDouble()
}

