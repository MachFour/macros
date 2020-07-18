package ingredients

import com.machfour.macros.cli.utils.CliUtils
import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.Schema
import com.machfour.macros.ingredients.CompositeFoodSpec
import com.machfour.macros.ingredients.IngredientSpec
import com.machfour.macros.ingredients.IngredientsParser
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.objects.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.sql.SQLException

import org.junit.jupiter.api.Assertions.*

class IngredientsTest {
    companion object {
        private val DB_LOCATION = "/home/max/devel/macros-kotlin/test/test-ingredients.sqlite"
        private lateinit var db: LinuxDatabase
        private val foodData: ColumnData<Food>? = null

        // the food that will be made up of the other two foods
        private val testCompositeFood: Food? = null

        // the foods that make up the composite foods
        private val testFood1: Food? = null
        private val testFood2: Food? = null

        // corresponding Ingredient Objects
        private val testIngredient1: Food? = null
        private val testIngredient2: Food? = null

        @BeforeAll
        @JvmStatic
        fun initDb() {
            db = LinuxDatabase.getInstance(DB_LOCATION)
            try {
                db.deleteByColumn(Food.table(), Schema.FoodTable.FOOD_TYPE, listOf(FoodType.COMPOSITE.niceName))
                db.clearTable(Ingredient.table())
            } catch (e: SQLException) {
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

        var recipe: CompositeFood? = null
        try {
            val recipes = IngredientsParser.createCompositeFoods(listOf(spec), db)
            assertEquals(1, recipes.size)
            recipe = recipes.iterator().next()
        } catch (e: SQLException) {
            fail<Any>("SQL exception processing composite food spec: $e")
        }

        assertNotNull(recipe)
        println("Nutrition data total")
        CliUtils.printNutritionData(recipe!!.getNutritionData(), false, System.out)
        println()
        println("Nutrition data per 100g")
        CliUtils.printNutritionData(recipe.getNutritionData().rescale(100.0), false, System.out)
    }


    @BeforeEach
    fun setUp() {
    }

    private fun clearFoodTable() {
        try {
            db.clearTable(Schema.FoodTable.instance)
        } catch (e: SQLException) {
            e.printStackTrace()
            fail<Any>("Deleting all foods threw SQL exception")
        }

    }


    @AfterEach
    fun tearDown() {
    }

}