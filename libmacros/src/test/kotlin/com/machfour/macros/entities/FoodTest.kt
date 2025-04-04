package com.machfour.macros.entities

import com.machfour.macros.foodname.indexNamePrototype
import kotlin.test.Test
import kotlin.test.assertEquals

class FoodTest {

    @Test
    fun testIndexNamePrototype() {
        val testCases = mapOf(
            listOf("name", null, null, null) to "name",
            listOf("name", null, null, "extraDesc") to "name-extraDesc",
            listOf("name", null, "variety", null) to "name-variety",
            listOf("name", "brand", "variety", null) to "name-brand-variety",
            listOf("name", null, "variety", "extraDesc") to "name-variety-extraDesc",
            listOf("name", "brand", null, "extraDesc") to "name-brand-extraDesc",
            listOf("name", "brand", "variety", "extraDesc") to "name-brand-variety-extraDesc",
            listOf("name-", null, null, null) to "name",
            listOf("--name", null, "variety--", "extraDesc---") to "name-variety-extraDesc",
            listOf("name", "()bran##d", "var~~~iet+y", null) to "name-brand-variety",
            listOf("name", "1 2-3,,4-,", null, "{}[]*()()\\``?$#$!@#$%^&***()") to "name-1-2-3-4",
        )

        for ((args, expected) in testCases) {
            val (name, brand, variety, extraDesc) = args
            requireNotNull(name)
            val actual = indexNamePrototype(name, brand, variety, extraDesc)
            assertEquals(expected, actual)
        }

        // removed chars: ()[]{}&%!$#@*^+=:;<>?/\
        // multiple spaces, dashes, commas replaced with dash
    }
}