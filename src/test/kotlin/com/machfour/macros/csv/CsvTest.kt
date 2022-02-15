package com.machfour.macros.csv

import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Serving
import com.machfour.macros.linux.LinuxConfig
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxDatabase.Companion.deleteIfExists
import com.machfour.macros.linux.LinuxDatabase.Companion.getInstance
import com.machfour.macros.queries.clearTable
import com.machfour.macros.queries.deleteAllIngredients
import com.machfour.macros.queries.getAllRawObjects
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.validation.SchemaViolation
import org.junit.jupiter.api.*
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.sql.SQLException

class CsvTest {
    companion object {
        lateinit var db: LinuxDatabase

        // TODO make test config
        val config: MacrosConfig = LinuxConfig()
        const val TEST_DB_LOCATION = "/home/max/devel/macros/test.sqlite"
        const val REAL_DB_LOCATION = "/home/max/devel/macros/macros.sqlite"
        const val TEST_WRITE_DIR = "/home/max/devel/macros/test"

        @JvmStatic
        @BeforeAll
        fun initDb() {
            try {
                db = getInstance(TEST_DB_LOCATION)
                deleteIfExists(TEST_DB_LOCATION)
                db.initDb(config.sqlConfig)
            } catch (e1: IOException) {
                e1.printStackTrace()
                Assertions.fail("Database initialisation threw IO exception")
            } catch (e2: SQLException) {
                e2.printStackTrace()
                Assertions.fail("Database initialisation threw SQL exception")
            }
        }

    }

    @BeforeEach
    fun clearDb() {
        deleteAllIngredients(db)
        clearTable(db, ServingTable)
        clearTable(db, FoodNutrientValue.table)
        clearTable(db, FoodTable)
    }

    @Test
    fun testCsvReadFoods() {
        var csvFoods: Map<String, Food>
        try {
            FileReader(config.foodCsvPath).use {
                csvFoods = buildFoodObjectTree(it, FoodTable.INDEX_NAME)
                Assertions.assertNotEquals(0, csvFoods.size, "CSV read in zero foods!")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        }
    }

    @Test
    fun testCsvReadServings() {
        var csvServings: List<Serving>
        try {
            FileReader(config.servingCsvPath).use {
                csvServings = buildServings(it, FoodTable.INDEX_NAME)
                Assertions.assertNotEquals(0, csvServings.size, "CSV read in zero servings!")
                println(csvServings[0])
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        }
    }

    @Test
    fun testCsvSaveFoods() {
        try {
            FileReader(config.foodCsvPath).use { importFoodData(db, it, FoodTable.INDEX_NAME,true) }
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        }
    }

    @Test
    fun testCsvSaveServings() {
        try {
            // save foods first
            FileReader(config.foodCsvPath).use { importFoodData(db, it, FoodTable.INDEX_NAME, true) }
            FileReader(config.servingCsvPath).use { importServings(db, it, FoodTable.INDEX_NAME, emptySet(), true) }
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: SchemaViolation) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        }
    }

    @Test
    fun testCsvWriteFoods() {
        try {
            val foods = getAllRawObjects(db, FoodTable)
            FileWriter("$TEST_WRITE_DIR/all-food.csv").use { writeObjectsToCsv(FoodTable, it, foods.values) }
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail("IOException was thrown")
        } catch (e2: SQLException) {
            e2.printStackTrace()
            Assertions.fail("Database save threw SQL exception")
        }
    }

    @Test
    fun testCsvWriteServings() {
        try {
            val servings = getAllRawObjects(db, ServingTable)
            FileWriter("$TEST_WRITE_DIR/all-serving.csv").use { writeObjectsToCsv(ServingTable, it, servings.values) }
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        }
    }

    // put this test after other imports
    @Test
    fun testCsvSaveRecipes() {
        try {
            FileReader(config.foodCsvPath).use { importFoodData(db, it, FoodTable.INDEX_NAME, true) }
            FileReader(config.recipeCsvPath).use { recipeCsv ->
                FileReader(config.ingredientsCsvPath).use { ingredientCsv ->
                    importRecipes(db, recipeCsv, ingredientCsv)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail("Exception was thrown")
        }
    }

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
}