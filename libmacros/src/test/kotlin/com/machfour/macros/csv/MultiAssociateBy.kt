package com.machfour.macros.csv

import kotlin.test.Test
import kotlin.test.assertEquals

class MultiAssociateBy {
    
    @Test
    fun testMultiAssociateBy() {
        val expected1 = mapOf(1 to listOf(1), 2 to listOf(2), 3 to listOf(3))
        val actual1 = listOf(1, 2, 3).multiAssociateBy { it }
        assertEquals(expected1, actual1, "expected = $expected1, actual = $actual1")

        val expected2 = emptyMap<Int, List<Int>>()
        val actual2 = emptyList<Int>().multiAssociateBy { it }
        assertEquals(expected2, actual2, "expected = $expected2, actual = $actual2")

        val expected3 = mapOf(1 to listOf(1, 1), 2 to listOf(2, 2), 3 to listOf(3, 3))
        val actual3 = listOf(1, 1, 2, 2, 3, 3).multiAssociateBy { it }
        assertEquals(expected3, actual3, "expected = $expected3, actual = $actual3")

        val expected4 = mapOf(null to listOf(null))
        val actual4 = listOf(null).multiAssociateBy { it }
        assertEquals(expected4, actual4, "expected = $expected4, actual = $actual4")
    }
}