package com.machfour.macros.util

fun <E, K> Iterable<E>.filterInSetUnlessEmpty(set: Set<K>, key: (E) -> K): List<E> {
    return if (set.isEmpty()) {
        if (this is List<E>) this else toList()
    } else {
        filter { set.contains(key(it)) }
    }
}

fun <E> Iterable<Set<E>>.unionAll(): Set<E> {
    return reduceOrNull { s, t -> s.union(t) } ?: emptySet()
}

fun <E> Iterable<Set<E>>.intersectAll(): Set<E> {
    return reduceOrNull { s, t -> s.intersect(t) } ?: emptySet()
}

fun <K, V, R> Map<K, V>.transformValueOrElse(key: K, elseValue: R, transform: (V) -> R): R {
    return when(val value = this[key]) {
        null -> elseValue
        else -> transform(value)
    }
}
