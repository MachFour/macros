package com.machfour.macros.sql

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SqlUtilsKtTest {

    private fun getInputLines(): Sequence<String> {
        return rawSqlSample.removePrefix("\n").split("\n").asSequence()
    }

    @Test
    fun testCreateSplitSqlStatements() {
        val expected = expectedSplitStatements
        val actual = createSplitSqlStatements(lines = getInputLines())
        assertEquals(expected, actual)
    }

    @Test
    fun testCreateSqlStatements() {
        val expected = expectedStatements
            .removePrefix("\n")
            .split("\n")
            .joinToString("@@@") { it }
        val got = createSqlStatements(
            lines = getInputLines(),
            lineSep = "@@@",
            commentMarker = "--"
        )
        assertEquals(expected, got)
    }
}