package com.machfour.macros.objects

import com.machfour.macros.csv.readFoodData
import com.machfour.macros.csv.saveImportedFoods
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxSqlConfig
import com.machfour.macros.nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.NutrientData
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.queries.getFoodByIndexName
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.SqlException
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLILITRES
import java.io.FileReader
import kotlin.test.*

private const val TEST_CSV_DIR = "/home/max/devel/macros/test/test-csv"
private const val TEST_FOOD_CSV = "$TEST_CSV_DIR/foods.csv"


class DensityTest {
    private lateinit var db: LinuxDatabase
    private lateinit var chickpeaFlour: Food
    private lateinit var water: Food
    private lateinit var chickpeaNd: NutrientData<FoodNutrientValue>
    private lateinit var waterNd: NutrientData<FoodNutrientValue>

    @BeforeTest
    fun initDb() {
        db = LinuxDatabase.getInstance("")
        db.openConnection(getGeneratedKeys = true)
        db.initDb(LinuxSqlConfig)

        val csvFoods = FileReader(TEST_FOOD_CSV).use {
            readFoodData(it.readText(), FoodTable.INDEX_NAME)
        }
        saveImportedFoods(db, csvFoods)


        val failMsg = "Could not find chickpea flour and water in DB. Has it been initialised with data?"
        try {
            chickpeaFlour = requireNotNull(getFoodByIndexName(db, "chickpea-flour")) { failMsg }
            water = requireNotNull(getFoodByIndexName(db, "water")) { failMsg }
            chickpeaNd = chickpeaFlour.nutrientData
            waterNd = water.nutrientData
            assertNotNull(water, failMsg)
            assertNotNull(chickpeaNd)
            assertNotNull(waterNd)
            assertNotNull(chickpeaFlour.density)
            assertNotNull(water.density)
        } catch (e: SqlException) {
            println(failMsg)
            fail(e.message)
        }
    }

    @AfterTest
    fun deInit() {
        db.closeConnection()
    }

    @Test
    fun testDensity() {
        val density = chickpeaFlour.density
        assertNotNull(density)
        val millilitresNd = chickpeaNd.rescale(100/density, MILLILITRES)
        val backConverted = millilitresNd.rescale100g()
        val carbs = CARBOHYDRATE
        println("Default quantity: " + chickpeaNd.perQuantity)
        println("mls quantity: " + millilitresNd.perQuantity)
        println("backConverted quantity: " + backConverted.perQuantity)
        assertEquals(chickpeaNd.amountOf(carbs)!!, millilitresNd.amountOf(carbs)!!, 0.01)
        assertEquals(chickpeaNd.amountOf(carbs)!!, backConverted.amountOf(carbs)!!, 0.01)
    }

    @Test
    fun testDensity2() {
        val density = chickpeaFlour.density
        assertNotNull(density)
        val chickPea100mL = chickpeaNd.rescale(100.0, MILLILITRES)
        val water100mL = waterNd.rescale(100.0, MILLILITRES)
        val combined = FoodNutrientData.sum(listOf(chickPea100mL, water100mL))
        assertEquals(GRAMS, combined.perQuantity.unit)
        assertEquals(100 + 100 * chickpeaFlour.density!!, combined.perQuantity.amount)
        assert(!combined.hasIncompleteData(QUANTITY))
    }

}