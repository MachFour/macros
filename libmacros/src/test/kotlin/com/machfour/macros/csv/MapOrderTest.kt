package com.machfour.macros.csv

import kotlin.test.Test

class MapOrderTest {

    @Test
    fun testMapEntryOrder() {
        val expectedEntries = listOf("a" to 1, "b" to 2, "c" to 3, "d" to 4)
        val expectedKeys = listOf("a", "b", "c", "d")
        val expectedValues = listOf(1, 2, 3, 4)

        val map = expectedEntries.toMap()

        assert(expectedKeys == map.keys.toList()) { "keys wrong: expected=$expectedKeys, actual=${map.keys}" }
        assert(expectedValues == map.values.toList()) { "values wrong: expected=$expectedValues, actual=${map.values}" }
    }
}