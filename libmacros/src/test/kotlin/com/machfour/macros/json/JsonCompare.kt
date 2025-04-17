package com.machfour.macros.json

import kotlinx.serialization.json.*

private fun compareJsonPrimitives(p1: JsonPrimitive, p2: JsonPrimitive): Int {
    if (p1 is JsonNull) {
        return if (p2 is JsonNull) 0 else -1
    }
    if (p2 is JsonNull) {
        return 1
    }
    return p1.content.compareTo(p2.content)
}

private fun compareJsonObjects(o1: JsonObject, o2: JsonObject): Int {
    if (o1.size != o2.size) {
        return o1.size.compareTo(o2.size)
    }

    val k1It = o1.keys.iterator()
    val k2It = o2.keys.iterator()
    while (k1It.hasNext() && k2It.hasNext()) {
        val k1 = k1It.next()
        val k2 = k2It.next()
        if (k1 != k2) {
            return k1.compareTo(k2)
        }
    }

    val e1It = o1.entries.iterator()
    val e2It = o2.entries.iterator()
    while (e1It.hasNext() && e2It.hasNext()) {
        val e1 = e1It.next()
        val e2 = e2It.next()
        val cmp = compareJsonElements(e1.value, e2.value)
        if (cmp != 0) {
            return cmp
        }
    }

    return 0
}

private fun compareJsonArrays(a1: JsonArray, a2: JsonArray): Int {
    if (a1.size != a2.size) {
        return a1.size.compareTo(a2.size)
    }

    for (i in a1.indices) {
        val cmp = compareJsonElements(a1[i], a2[i])
        if (cmp != 0) {
            return cmp
        }
    }

    return 0
}

fun compareJsonElements(e1: JsonElement, e2: JsonElement): Int {
    if (e1 is JsonPrimitive && e2 is JsonPrimitive) {
        return compareJsonPrimitives(e1, e2)
    }
    if (e1 is JsonObject && e2 is JsonObject) {
        return compareJsonObjects(e1, e2)
    }
    if (e1 is JsonArray && e2 is JsonArray) {
        return compareJsonArrays(e1, e2)
    }
    // e1 and e2 are not the same. Sort in order: Primitive < Object < Array
    return when (e1) {
        is JsonPrimitive -> -1
        is JsonObject -> if (e2 is JsonPrimitive) 1 else -1
        is JsonArray -> 1
    }
}

val JsonElementComparator = Comparator<JsonElement> { e1, e2 -> compareJsonElements(e1, e2) }

// recursively sorts fields of a JsonElement by key
fun JsonElement.normalize(): JsonElement {
    return when (this) {
        is JsonPrimitive -> this
        is JsonObject -> JsonObject(map { it.key to it.value.normalize() }.sortedBy { it.first }.toMap())
        is JsonArray -> JsonArray(map { it.normalize() }.sortedWith(JsonElementComparator))
    }
}

