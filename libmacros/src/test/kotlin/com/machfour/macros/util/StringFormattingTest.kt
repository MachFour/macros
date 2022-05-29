package com.machfour.macros.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StringFormattingTest {

    @Test
    fun testDoubleToString() {
        assertEquals("0.0", 0.0.toString(1))
        assertEquals("0.00", 0.0.toString(2))
        assertEquals("0.000", 0.0.toString(3))
        assertEquals("1.000", 1.0.toString(3))
        assertEquals("1.111", 1.111.toString(3))
        assertEquals("1.123", 1.1233344.toString(3))
        assertEquals("1.124", 1.1235.toString(3))
        assertEquals("10.0", 9.99.toString(1))
        assertEquals("0.75", 0.749.toString(2))
        assertEquals("10000000000000.0", 9999999999999.99.toString(1))
        assertEquals(">9223372036854775807", 1.7976931348623157E308.toString(0))
        assertEquals("<-9223372036854775808", (-1.7976931348623157E308).toString(0))
        assertEquals("9223372036854.770", 9223372036854.77.toString(3))
        assertEquals("-9223372036854.770", (-9223372036854.77).toString(3))
    }

    @Test
    fun testDoubleToStringTrimTrailingZeros() {
        assertEquals("0", 0.0.toString(1, trimTrailingZeros = true))
        assertEquals("0", 0.0.toString(2, trimTrailingZeros = true))
        assertEquals("0.1", 0.10.toString(3, trimTrailingZeros = true))
        assertEquals("1", 1.0.toString(3, trimTrailingZeros = true))
        assertEquals("1.111", 1.111.toString(3, trimTrailingZeros = true))
        assertEquals("1.123", 1.1233344.toString(3, trimTrailingZeros = true))
        assertEquals("1.124", 1.1235.toString(3, trimTrailingZeros = true))
        assertEquals("10", 9.99.toString(1, trimTrailingZeros = true))
        assertEquals("0.75", 0.749.toString(2, trimTrailingZeros = true))
        assertEquals("10000000000000", 9999999999999.99.toString(1, trimTrailingZeros = true))
        assertEquals(">9223372036854775807", 1.7976931348623157E308.toString(0, trimTrailingZeros = true))
        assertEquals("<-9223372036854775808", (-1.7976931348623157E308).toString(0, trimTrailingZeros = true))
        assertEquals("9223372036854.77", 9223372036854.77.toString(3, trimTrailingZeros = true))
        assertEquals("-9223372036854.77", (-9223372036854.77).toString(3, trimTrailingZeros = true))
    }

    @Test
    fun testStringFixedWidth() {
        assertEquals("   3", 3.toString().fmt(4))
        assertEquals("3   ", 3.toString().fmt(4, leftAlign = true))
        assertEquals("0003", 3.toString().fmt(4, leftAlign = false, padChar = '0'))
        assertEquals("3000", 3000.toString().fmt(4, leftAlign = false, padChar = '0'))
        assertEquals("3000", 3000.toString().fmt(1, leftAlign = false, padChar = '0'))
        assertEquals("3000", 3000.toString().fmt(0, leftAlign = true, padChar = 'J'))
        assertEquals("3000", 3000.toString().fmt(0, leftAlign = false, padChar = '@'))
    }
}