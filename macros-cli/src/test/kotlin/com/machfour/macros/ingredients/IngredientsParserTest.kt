package com.machfour.macros.ingredients

import com.machfour.macros.csv.readFoodData
import com.machfour.macros.csv.saveImportedFoods
import com.machfour.macros.entities.CompositeFood
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.SqlException
import java.io.FileReader
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail

private const val TEST_CSV_DIR = "/home/max/devel/macros/test/test-csv"
private const val TEST_FOOD_CSV = "$TEST_CSV_DIR/foods.csv"

class IngredientsParserTest {

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
    fun deserialise() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val ingredientSpecs = deserialiseIngredientsJson(r)
                @Suppress("UNUSED")
                val newFoods: Collection<CompositeFood> = createCompositeFoods(ingredientSpecs, db)
                println("Composite Foods Read:")
                println(ingredientSpecs.joinToString("\n"))
            }
        } catch (e: IOException) {
            fail(e.message)
        } catch (e: SqlException) {
            fail(e.message)
        }
    }

    @Test
    fun testCreate() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val ingredientSpecs = deserialiseIngredientsJson(r)
                val newFoods = createCompositeFoods(ingredientSpecs, db)
                println("Composite Foods created:")
                for (f in newFoods) {
                    println(f)
                }
            }
        } catch (e: IOException) {
            fail(e.message)
        } catch (e: SqlException) {
            fail(e.message)
        }
    }

    @Test
    fun testSave() {
        try {
            FileReader("/home/max/devel/macros-test-data/mayo-recipes.json").use { r ->
                val recipes = readRecipes(r, db)
                saveRecipes(recipes, db)
            }
        } catch (e: IOException) {
            fail(e.message)
        } catch (e: SqlException) {
            fail(e.message)
        }
    }

}