package com.machfour.macros.csv

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.core.EntityId
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Serving
import com.machfour.macros.linux.LinuxConfig
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxDatabase.Companion.getInstance
import com.machfour.macros.queries.getAllRawObjects
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.validation.SchemaViolation
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import kotlin.test.*

const val TEST_WRITE_DIR = "/home/max/devel/macros/test"

class CsvTest {
    private lateinit var db: LinuxDatabase

    // TODO make test config
    private val config: CliConfig = LinuxConfig()

    @BeforeTest
    fun init() {
        db = getInstance("").apply {
            openConnection(getGeneratedKeys = true)
            initDb(config.sqlConfig)
        }
    }

    @AfterTest
    fun deInit() {
        db.closeConnection()
    }


    @Test
    fun testCsvReadFoods() {
        var csvFoods: Map<String, Food>
        try {
            FileReader(config.foodCsvPath).use {
                csvFoods = buildFoodObjectTree(it.readText(), FoodTable.INDEX_NAME)
                assertNotEquals(0, csvFoods.size, "CSV read in zero foods!")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            fail("Exception was thrown: $e")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            fail("Exception was thrown: $e")
        }
    }

    @Test
    fun testCsvReadServings() {
        var csvServings: List<Pair<Serving, String>>
        try {
            FileReader(config.servingCsvPath).use {
                csvServings = buildServingsWithFoodKeys(it.readText(), FoodTable.INDEX_NAME)
                assertNotEquals(0, csvServings.size, "CSV read in zero servings!")
                println(csvServings[0])
            }
        } catch (e: CsvException) {
            e.printStackTrace()
            fail("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            fail("Exception was thrown")
        }
    }

    @Test
    fun testCsvSaveFoods() {
        try {
            FileReader(config.foodCsvPath).use {
                val csvFoods = readFoodData(it.readText(), FoodTable.INDEX_NAME)
                saveImportedFoods(db, csvFoods)
            }
        } catch (e: SqlException) {
            e.printStackTrace()
            fail("Exception was thrown")
        } catch (e: CsvException) {
            e.printStackTrace()
            fail("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            fail("Exception was thrown")
        }
    }

    @Test
    fun testCsvSaveServings() {
        try {
            // save foods first
            val indexNameToId: Map<String, EntityId>
            FileReader(config.foodCsvPath).use {
                val csvFoods = readFoodData(it.readText(), FoodTable.INDEX_NAME)
                val (foodKeyToId, _) = saveImportedFoods(db, csvFoods)
                indexNameToId = foodKeyToId
            }
            FileReader(config.servingCsvPath).use {
                importServings(db, it.readText(), FoodTable.INDEX_NAME, indexNameToId, false)
            }
        } catch (e: SqlException) {
            e.printStackTrace()
            fail("Exception was thrown")
        } catch (e: CsvException) {
            e.printStackTrace()
            fail("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            fail("Exception was thrown")
        } catch (e: SchemaViolation) {
            e.printStackTrace()
            fail("Exception was thrown")
        }
    }

    @Test
    fun testCsvWriteFoods() {
        try {
            val foods = getAllRawObjects(db, FoodTable)
            FileWriter("$TEST_WRITE_DIR/all-food.csv").use {
                it.write(writeObjectsToCsv(FoodTable, foods.values))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            fail("IOException was thrown")
        } catch (e2: SqlException) {
            e2.printStackTrace()
            fail("Database save threw SQL exception")
        }
    }

    @Test
    fun testCsvWriteServings() {
        try {
            val servings = getAllRawObjects(db, ServingTable)
            FileWriter("$TEST_WRITE_DIR/all-serving.csv").use {
                it.write(writeObjectsToCsv(ServingTable, servings.values))
            }
        } catch (e: SqlException) {
            e.printStackTrace()
            fail("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            fail("Exception was thrown")
        }
    }

    // put this test after other imports
    @Test
    fun testCsvSaveRecipes() {
        try {
            FileReader(config.foodCsvPath).use {
                val csvFoods = readFoodData(it.readText(), FoodTable.INDEX_NAME)
                saveImportedFoods(db, csvFoods)
            }
            FileReader(config.recipeCsvPath).use { recipeCsv ->
                FileReader(config.ingredientsCsvPath).use { ingredientCsv ->
                    importRecipes(db, recipeCsv.readText(), ingredientCsv.readText())
                }
            }
        } catch (e: SqlException) {
            e.printStackTrace()
            fail("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            fail("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            fail("Exception was thrown")
        }
    }
}