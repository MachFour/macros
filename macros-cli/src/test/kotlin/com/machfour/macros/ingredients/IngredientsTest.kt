package com.machfour.macros.ingredients

import com.machfour.macros.cli.utils.printNutrientData
import com.machfour.macros.csv.readFoodData
import com.machfour.macros.csv.saveImportedFoods
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.SqlException
import java.io.FileReader
import kotlin.test.*

private const val TEST_CSV_DIR = "/home/max/devel/macros/test/test-csv"
private const val TEST_FOOD_CSV = "$TEST_CSV_DIR/foods.csv"

class IngredientsTest {
    private lateinit var db: LinuxDatabase

    @BeforeTest
    fun init() {
        db = LinuxDatabase.getInstance("").apply {
            openConnection(getGeneratedKeys = true)
            initDb(LinuxSqlConfig)
        }

        val csvFoods = FileReader(TEST_FOOD_CSV).use {
            readFoodData(it.readText(), FoodTable.INDEX_NAME)
        }
        saveImportedFoods(db, csvFoods)
    }

    @AfterTest
    fun deInit() {
        db.closeConnection()
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
            fail("SQL exception processing composite food spec: $e")
        }

        checkNotNull(recipe)
        println("Nutrition data total")
        printNutrientData(recipe.nutrientData, false)
        println()
        println("Nutrition data per 100g")
        val rescaled = recipe.nutrientData.rescale100g()
        printNutrientData(rescaled, false)
    }
}