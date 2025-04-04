package com.machfour.macros.formatting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringFormattingTest {
    @Test
    fun testDoubleToStringWithTrailingZeros() {
        assertEquals("0.0", 0.0.toString(1, withTrailingZeros = true))
        assertEquals("0.00", 0.0.toString(2, withTrailingZeros = true))
        assertEquals("0.000", 0.0.toString(3, withTrailingZeros = true))
        assertEquals("1.000", 1.0.toString(3, withTrailingZeros = true))
        assertEquals("1.111", 1.111.toString(3, withTrailingZeros = true))
        assertEquals("1.123", 1.1233344.toString(3, withTrailingZeros = true))
        assertEquals("1.124", 1.1235.toString(3, withTrailingZeros = true))
        assertEquals("10.0", 9.99.toString(1, withTrailingZeros = true))
        assertEquals("0.75", 0.749.toString(2, withTrailingZeros = true))
        assertEquals("10000000000000.0", 9999999999999.99.toString(1, withTrailingZeros = true))
        assertEquals("9223372036854.770", 9223372036854.77.toString(3, withTrailingZeros = true))
        assertEquals("-9223372036854.770", (-9223372036854.77).toString(3, withTrailingZeros = true))
    }

    @Test
    fun testDoubleToString() {
        assertEquals("0", 0.0.toString(1, withTrailingZeros = false))
        assertEquals("0", 0.0.toString(2, withTrailingZeros = false))
        assertEquals("0.1", 0.10.toString(3, withTrailingZeros = false))
        assertEquals("0.01", 0.01.toString(2, withTrailingZeros = false))
        assertEquals("0.01", 0.01.toString(3, withTrailingZeros = false))
        assertEquals("0.001", 0.001.toString(3, withTrailingZeros = false))
        assertEquals("0.001", 0.001.toString(4, withTrailingZeros = false))
        assertEquals("0.0001", 0.0001.toString(4, withTrailingZeros = false))
        assertEquals("0.0001", 0.0001.toString(5, withTrailingZeros = false))
        assertEquals("0", 0.00000001.toString(5, withTrailingZeros = false))
        assertEquals("1", 1.0.toString(3, withTrailingZeros = false))
        assertEquals("1.111", 1.111.toString(3, withTrailingZeros = false))
        assertEquals("1.123", 1.1233344.toString(3, withTrailingZeros = false))
        assertEquals("1.124", 1.1235.toString(3, withTrailingZeros = false))
        assertEquals("10", 9.99.toString(1, withTrailingZeros = false))
        assertEquals("0.75", 0.749.toString(2, withTrailingZeros = false))
        assertEquals("10000000000000", 9999999999999.99.toString(1, withTrailingZeros = false))
        assertEquals("9223372036854.77", 9223372036854.77.toString(3, withTrailingZeros = false))
        assertEquals("-9223372036854.77", (-9223372036854.77).toString(3, withTrailingZeros = false))
        // Difficult test case
        assertEquals("295.34", 295.3350.toString(2, withTrailingZeros = false))
    }

    @Test
    fun testFloatToStringExtraPrecision() {
        assertEquals("0.0000000", 0.0f.toString(7))
        assertEquals("0.00", 0.0f.toString(2))
        assertEquals("0.01", 0.01f.toString(2))
        assertEquals("0.010", 0.01f.toString(3))
        assertEquals("0.001", 0.001f.toString(3))
        assertEquals("0.0010", 0.001f.toString(4))
        assertEquals("0.0001", 0.0001f.toString(4))
        assertEquals("0.00010", 0.0001f.toString(5))
        assertEquals("0.0000", 0.00001f.toString(4))
        assertEquals("0.1000", 0.10f.toString(4))
        assertEquals("1.000", 1.0f.toString(3))
        assertEquals("1.0000", 1f.toString(4))
        assertEquals("1.111000", 1.111f.toString(6))
        assertEquals("1.123", 1.1233344f.toString(3))
        assertEquals("1.124", 1.1236f.toString(3))
        assertEquals("10", 9.99f.toString(0))
        assertEquals("0.75", 0.749f.toString(2))
    }

    @Test
    fun testStringFixedWidth() {
        assertEquals("   3", 3.toString().fmt(4))
        assertEquals("3   ", 3.toString().fmt(4, alignLeft = true))
        assertEquals("0003", 3.toString().fmt(4, alignLeft = false, padChar = '0'))
        assertEquals("3000", 3000.toString().fmt(4, alignLeft = false, padChar = '0'))
        assertEquals("3000", 3000.toString().fmt(1, alignLeft = false, padChar = '0'))
        assertEquals("3000", 3000.toString().fmt(0, alignLeft = true, padChar = 'J'))
        assertEquals("3000", 3000.toString().fmt(0, alignLeft = false, padChar = '@'))
    }
}