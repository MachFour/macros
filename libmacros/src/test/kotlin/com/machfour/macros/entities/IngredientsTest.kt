package com.machfour.macros.entities

import com.machfour.macros.sample.exampleRecipe1
import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientsTest {

    @Test
    fun testExampleRecipe() {
        val recipe = exampleRecipe1
        for (i in recipe.ingredients) {
            println(i)
        }

        println(recipe.nutrientData)
        assertEquals(recipe.shortName, "recipe 1")
    }
}