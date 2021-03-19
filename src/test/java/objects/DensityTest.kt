package objects

import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxDatabase.Companion.getInstance
import com.machfour.macros.entities.*
import com.machfour.macros.entities.inbuilt.Nutrients
import com.machfour.macros.entities.inbuilt.Units
import com.machfour.macros.nutrientdata.FoodNutrientData
import com.machfour.macros.queries.FoodQueries
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
                Assertions.fail<Any>(e)
            }
        }
    }
    @Test
    fun testDensity() {
        val density = chickpeaFlour.density
        Assertions.assertNotNull(density)
        requireNotNull(density)
        val millilitresNd = chickpeaNd.withQuantityUnit(Units.MILLILITRES, density).rescale(100 / density)
        val backConverted = millilitresNd.withQuantityUnit(Units.GRAMS, density).rescale100()
        val carbs = Nutrients.CARBOHYDRATE
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
        val chickPea100mL = chickpeaNd.withQuantityUnit(Units.MILLILITRES, density).rescale100()
        val water100mL = waterNd.withQuantityUnit(Units.MILLILITRES, 1.0).rescale100()
        val combined = FoodNutrientData.sum(listOf(chickPea100mL, water100mL), listOf(density, 1.0))
        Assertions.assertEquals(Units.GRAMS, combined.quantityObj.unit)
        Assertions.assertEquals(100 + 100 * chickpeaFlour.density!!, combined.quantityObj.value)
        Assertions.assertTrue(combined.hasCompleteData(Nutrients.QUANTITY))
    }

}