package com.machfour.macros.ingredients

import com.machfour.macros.cli.utils.printNutrientData
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.queries.clearTable
import com.machfour.macros.queries.deleteAllCompositeFoods
import com.machfour.macros.queries.deleteAllIngredients
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.SqlException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IngredientsTest {
    companion object {
        private const val DB_LOCATION = "/home/max/devel/macros/test/test-ingredients.sqlite"
        private lateinit var db: LinuxDatabase

        @BeforeAll
        @JvmStatic
        fun initDb() {
            db = LinuxDatabase.getInstance(DB_LOCATION)
            try {
                deleteAllIngredients(db)
                deleteAllCompositeFoods(db)
            } catch (e: SqlException) {
                println("Could not delete existing composite foods and/or clear ingredients table!")
                fail<Any>(e)
            }

        }
    }

    @Test
    fun testNutritionSum() {
        val spec = CompositeFoodSpec("chickpea-bread", "Chickpea bread", null, null)
        val waterSpec = IngredientSpec("water", 875.0, "ml", "3.5 cups")
        val chickpeaSpec = IngredientSpec("chickpea-flour", 625.0, "ml", "2.5 cups")
        //IngredientSpec chickpeaSpec = new IngredientSpec("chickpea-flour", 306.0, "g", "2.5 cups");
        val cookingSpec = IngredientSpec("water", -493.0, "g", "cooked weight")
        val oilSpec = IngredientSpec("olive-oil-cobram", 60.0, "ml", null)
        spec.addIngredients(listOf(waterSpec, chickpeaSpec, cookingSpec, oilSpec))

        val recipe = try {
            val recipes = createCompositeFoods(listOf(spec), db)
            assertEquals(1, recipes.size)
            recipes.firstOrNull()
        } catch (e: SqlException) {
            fail<Any>("SQL exception processing composite food spec: $e")
            null
        }

        checkNotNull(recipe)
        println("Nutrition data total")
        printNutrientData(recipe.nutrientData, false)
        println()
        println("Nutrition data per 100g")
        val rescaled = recipe.nutrientData.rescale100()
        printNutrientData(rescaled, false)
    }


    @BeforeEach
    fun setUp() {
    }

    private fun clearFoodTable() {
        try {
            clearTable(db, FoodTable)
        } catch (e: SqlException) {
            e.printStackTrace()
            fail<Any>("Deleting all foods threw SQL exception")
        }

    }


    @AfterEach
    fun tearDown() {
    }

}