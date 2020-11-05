package objects

import com.machfour.macros.core.NutrientData
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxDatabase.Companion.getInstance
import com.machfour.macros.objects.*
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Units
import com.machfour.macros.queries.FoodQueries
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class DensityTest {
    companion object {
        private const val DB_LOCATION = "/home/max/devel/macros-kotlin/test.sqlite"
        lateinit var db: LinuxDatabase
        lateinit var chickpeaFlour: Food
        lateinit var water: Food
        lateinit var chickpeaNd: NutrientData
        lateinit var waterNd: NutrientData

        @BeforeAll
        @JvmStatic
        fun initDb() {
            db = getInstance(DB_LOCATION)
            val failMsg = "Could not find chickpea flour and water in DB. Has it been initialised with data?"
            try {
                chickpeaFlour = requireNotNull(FoodQueries.getFoodByIndexName(db, "chickpea-flour")) { failMsg }
                water = requireNotNull(FoodQueries.getFoodByIndexName(db, "water")) { failMsg }
                chickpeaNd = chickpeaFlour.getNutritionData().nutrientData
                waterNd = water.getNutritionData().nutrientData
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
        val millilitresNd = NutritionCalculations.rescale(chickpeaNd, 100 / density!!, Units.MILLILITRES, chickpeaFlour.density)
        val backConverted = NutritionCalculations.rescale(millilitresNd, 100.0, Units.GRAMS, chickpeaFlour.density)
        val carbs = Nutrients.CARBOHYDRATE
        println("Default quantity: " + chickpeaNd.quantityObj)
        println("mls quantity: " + millilitresNd.quantityObj)
        println("backConverted quantity: " + backConverted.quantityObj)
        Assertions.assertEquals(chickpeaNd.amountOf(carbs)!!, millilitresNd.amountOf(carbs)!!, 0.01)
        Assertions.assertEquals(chickpeaNd.amountOf(carbs)!!, backConverted.amountOf(carbs)!!, 0.01)
    }

    @Test
    fun testDensity2() {
        val chickPea100mL = NutritionCalculations.rescale(chickpeaNd, 100.0, Units.MILLILITRES, chickpeaFlour.density)
        val water100mL = NutritionCalculations.rescale(waterNd, 100.0, Units.MILLILITRES, 1.0)
        val combined = NutritionCalculations.sum(listOf(chickPea100mL, water100mL), listOf(chickpeaFlour.density!!, 1.0))
        Assertions.assertEquals(Units.GRAMS, combined.quantityObj.unit)
        Assertions.assertEquals(100 + 100 * chickpeaFlour.density!!, combined.quantityObj.value)
        Assertions.assertTrue(combined.hasCompleteData(Nutrients.QUANTITY))
    }

}