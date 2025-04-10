package com.machfour.macros.ingredients

import com.machfour.macros.csv.readFoodData
import com.machfour.macros.csv.saveImportedFoods
import com.machfour.macros.entities.Food
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.queries.getFoodByIndexName
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.SqlException
import java.io.FileReader
import java.io.IOException
import kotlin.test.*

private const val TEST_CSV_DIR = "/home/max/devel/macros/test/test-csv"
private const val TEST_FOOD_CSV = "$TEST_CSV_DIR/foods.csv"

class IngredientsRollbackTest {
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

    // the ingredients list has invalid quantity units, so we expect that the composite food
    // should not be saved either even though it is inserted first
    @Test
    fun testRollback() {
        var indexName = ""
        try {
            val f: Food
            FileReader("/home/max/devel/macros-test-data/valid-food-invalid-ingredients.json").use { r ->
                val foods = readRecipes(r, db)

                assertEquals(1, foods.size)
                assertNotNull(foods[0])
                f = foods[0]
                indexName = f.indexName

                saveRecipes(foods, db)
                fail("saveRecipes() did not throw an SqlException")
            }
        } catch (e1: IOException) {
            fail(e1.message)
        } catch (e2: SqlException) {
            // we expect a foreign key constraint failure, do nothing
        }

        try {
            val f = getFoodByIndexName(db, indexName)
            assertNull(f, "Composite food was saved in the database, but should not have been")
        } catch (e2: SqlException) {
            fail(e2.message)
        }
    }

}

