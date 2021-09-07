package com.machfour.macros.objects

import com.machfour.macros.entities.Food
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxDatabase.Companion.getInstance
import com.machfour.macros.nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLILITRES
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class DensityTest {
    companion object {
        private const val DB_LOCATION = "/home/max/devel/macros/test.sqlite"
        lateinit var db: LinuxDatabase
        lateinit var chickpeaFlour: Food
        lateinit var water: Food
        lateinit var chickpeaNd: FoodNutrientData
        lateinit var waterNd: FoodNutrientData

        @BeforeAll
        @JvmStatic
        fun initDb() {
            db = getInstance(DB_LOCATION)
            val failMsg = "Could not find chickpea flour and water in DB. Has it been initialised with data?"
            try {
                chickpeaFlour = requireNotNull(FoodQueries.getFoodByIndexName(db, "chickpea-flour")) { failMsg }
                water = requireNotNull(FoodQueries.getFoodByIndexName(db, "water")) { failMsg }
                chickpeaNd = chickpeaFlour.nutrientData
                waterNd = water.nutrientData
                Assertions.assertNotNull(water, failMsg)
                Assertions.assertNotNull(chickpeaNd)
                Assertions.assertNotNull(waterNd)
                Assertions.assertNotNull(chickpeaFlour.density)
                Assertions.assertNotNull(water.density)
            } catch (e: SQLException) {
                println(failMsg)
                Assertions.fail(e)
            }
        }
    }

    @Test
    fun testDensity() {
        val density = chickpeaFlour.density
        Assertions.assertNotNull(density)
        requireNotNull(density)
        val millilitresNd = chickpeaNd.withQuantityUnit(MILLILITRES, density).rescale(100 / density)
        val backConverted = millilitresNd.withQuantityUnit(GRAMS, density).rescale100()
        val carbs = CARBOHYDRATE
        println("Default quantity: " + chickpeaNd.quantityObj)
        println("mls quantity: " + millilitresNd.quantityObj)
        println("backConverted quantity: " + backConverted.quantityObj)
        Assertions.assertEquals(chickpeaNd.amountOf(carbs)!!, millilitresNd.amountOf(carbs)!!, 0.01)
        Assertions.assertEquals(chickpeaNd.amountOf(carbs)!!, backConverted.amountOf(carbs)!!, 0.01)
    }

    @Test
    fun testDensity2() {
        val density = chickpeaFlour.density
        Assertions.assertNotNull(density)
        requireNotNull(density)
        val chickPea100mL = chickpeaNd.withQuantityUnit(MILLILITRES, density).rescale100()
        val water100mL = waterNd.withQuantityUnit(MILLILITRES, 1.0).rescale100()
        val combined = FoodNutrientData.sum(listOf(chickPea100mL, water100mL), listOf(density, 1.0))
        Assertions.assertEquals(GRAMS, combined.quantityObj.unit)
        Assertions.assertEquals(100 + 100 * chickpeaFlour.density!!, combined.quantityObj.value)
        Assertions.assertTrue(combined.hasCompleteData(QUANTITY))
    }

}