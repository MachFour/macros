package storage

import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.linux.LinuxConfig
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxDatabase.Companion.deleteIfExists
import com.machfour.macros.linux.LinuxDatabase.Companion.getInstance
import com.machfour.macros.objects.*
import com.machfour.macros.queries.FoodPortionQueries
import com.machfour.macros.storage.CsvBackup.writeObjectsToCsv
import com.machfour.macros.storage.CsvImport.buildFoodObjectTree
import com.machfour.macros.storage.CsvImport.buildServings
import com.machfour.macros.storage.CsvImport.importFoodData
import com.machfour.macros.storage.CsvImport.importRecipes
import com.machfour.macros.storage.CsvImport.importServings
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
                db.initDb()
            } catch (e1: IOException) {
                e1.printStackTrace()
                Assertions.fail<Any>("Database initialisation threw IO exception")
            } catch (e2: SQLException) {
                e2.printStackTrace()
                Assertions.fail<Any>("Database initialisation threw SQL exception")
            }
        }

    }

    @BeforeEach
    fun clearDb() {
        FoodPortionQueries.deleteAllIngredients(db)
        db.clearTable(Serving.table)
        db.clearTable(NutrientValue.table)
        db.clearTable(Food.table)
    }

    @Test
    fun testCsvReadFoods() {
        var csvFoods: Map<String, Food>
        try {
            FileReader(config.foodCsvPath).use {
                csvFoods = buildFoodObjectTree(it)
                Assertions.assertNotEquals(0, csvFoods.size, "CSV read in zero foods!")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        }
    }

    @Test
    fun testCsvReadServings() {
        var csvServings: List<Serving>
        try {
            FileReader(config.servingCsvPath).use {
                csvServings = buildServings(it)
                Assertions.assertNotEquals(0, csvServings.size, "CSV read in zero servings!")
                println(csvServings[0])
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        }
    }

    @Test
    fun testCsvSaveFoods() {
        try {
            FileReader(config.foodCsvPath).use { importFoodData(db, it, true) }
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        }
    }

    @Test
    fun testCsvSaveServings() {
        try {
            // save foods first
            FileReader(config.foodCsvPath).use { importFoodData(db, it, true) }
            FileReader(config.servingCsvPath).use { importServings(db, it, true) }
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: SchemaViolation) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        }
    }

    @Test
    fun testCsvWriteFoods() {
        try {
            val foods = db.getAllRawObjects(Food.table)
            FileWriter("$TEST_WRITE_DIR/all-food.csv").use { writeObjectsToCsv(Food.table, it, foods.values) }
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail<Any>("IOException was thrown")
        } catch (e2: SQLException) {
            e2.printStackTrace()
            Assertions.fail<Any>("Database save threw SQL exception")
        }
    }

    @Test
    fun testCsvWriteServings() {
        try {
            val servings = db.getAllRawObjects(Serving.table)
            FileWriter("$TEST_WRITE_DIR/all-serving.csv").use { writeObjectsToCsv(Serving.table, it, servings.values) }
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        }
    }

    // put this test after other imports
    @Test
    fun testCsvSaveRecipes() {
        try {
            FileReader(config.foodCsvPath).use { importFoodData(db, it, true) }
            FileReader(config.recipeCsvPath).use { recipeCsv ->
                FileReader(config.ingredientsCsvPath).use { ingredientCsv ->
                    importRecipes(db, recipeCsv, ingredientCsv)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: IOException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        } catch (e: TypeCastException) {
            e.printStackTrace()
            Assertions.fail<Any>("Exception was thrown")
        }
    }

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
}