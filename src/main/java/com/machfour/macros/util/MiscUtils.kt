package com.machfour.macros.util

import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

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

fun <E, K> List<E>.filterInSetUnlessEmpty(set: Set<K>, key: (E) -> K) : List<E> {
    return if (set.isEmpty()) this else this.filter { set.contains(key(it)) }
}

fun <K, V, R> mapValueOrElse(data: Map<K, V>, key: K, elseValue: R, mapping: (V) -> R): R {
    return when(val value = data[key]) {
        null -> elseValue
        else -> mapping(value)
    }
}

@Suppress("unused")
fun <E> Iterable<Set<E>>.unionAll() : Set<E> {
    return reduce { s, t -> s.union(t) }
}

@Suppress("unused")
fun <E> Iterable<Set<E>>.intersectAll() : Set<E> {
    return reduce { s, t -> s.intersect(t) }
}

fun <E> Iterable<Set<E>>.unionAllOrNull() : Set<E>? {
    return reduceOrNull { s, t -> s.union(t) }
}

fun <E> Iterable<Set<E>>.intersectAllOrNull() : Set<E>? {
    return reduceOrNull { s, t -> s.intersect(t) }
}

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
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

