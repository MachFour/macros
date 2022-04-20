package com.machfour.macros.util

fun <E, K> Iterable<E>.filterInSetUnlessEmpty(set: Set<K>, produceKey: (E) -> K): List<E> {
    return if (set.isEmpty()) {
        if (this is List<E>) this else toList()
    } else {
        filter { set.contains(produceKey(it)) }
    }
}

fun <E, K> Iterable<E>.filterAnyInSetUnlessEmpty(set: Set<K>, produceKeys: (E) -> Collection<K>): List<E> {
    return if (set.isEmpty()) {
        if (this is List<E>) this else toList()
    } else {
        filter { produceKeys(it).any { key -> set.contains(key) } }
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
