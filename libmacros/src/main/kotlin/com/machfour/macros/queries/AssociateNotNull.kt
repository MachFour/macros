package com.machfour.macros.queries

inline fun <K, V> Collection<K>.associateNotNullWith(valueSelector: (K) -> V?): Map<K, V> {
    val result = LinkedHashMap<K, V>((size*4/3+1).coerceAtLeast(16))
    return associateNotNullWithTo(result, valueSelector)
}

inline fun <K, V, M : MutableMap<in K, in V>> Iterable<K>.associateNotNullWithTo(destination: M, valueSelector: (K) -> V?): M {
    for (element in this) {
        val value = valueSelector(element)
        if (value != null) {
            destination.put(element, value)
        }
    }
    return destination
}
