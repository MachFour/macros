package objects

import com.machfour.macros.core.Schema.NutritionDataTable
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.linux.LinuxDatabase.Companion.getInstance
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.NutritionCalculations
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.Units
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
        lateinit var chickpeaNd: NutritionData
        lateinit var waterNd: NutritionData

        @BeforeAll
        @JvmStatic
        fun initDb() {
            db = getInstance(DB_LOCATION)
            val failMsg = "Could not find chickpea flour and water in DB. Has it been initialised with data?"
            try {
                chickpeaFlour = requireNotNull(FoodQueries.getFoodByIndexName(db, "chickpea-flour")) { failMsg }
                water = requireNotNull(FoodQueries.getFoodByIndexName(db, "water")) { failMsg }
                chickpeaNd = chickpeaFlour.getNutritionData()
                waterNd = water.getNutritionData()
                Assertions.assertNotNull(water, failMsg)
                Assertions.assertNotNull(chickpeaNd)
                Assertions.assertNotNull(waterNd)
                Assertions.assertNotNull(chickpeaNd.density)
                Assertions.assertNotNull(waterNd.density)
            } catch (e: SQLException) {
                println(failMsg)
                Assertions.fail<Any>(e)
            }
        }
    }
    @Test
    fun testDensity() {
        val density = chickpeaNd.density
        Assertions.assertNotNull(density)
        val millilitresNd = NutritionCalculations.rescale(chickpeaNd, 100 / density!!, Units.MILLILITRES)
        val backConverted = NutritionCalculations.rescale(millilitresNd, 100.0, Units.GRAMS)
        val carbs = NutritionDataTable.CARBOHYDRATE
        Assertions.assertEquals(chickpeaNd.getData(carbs)!!, millilitresNd.getData(carbs)!!, 0.01)
        Assertions.assertEquals(chickpeaNd.getData(carbs)!!, backConverted.getData(carbs)!!, 0.01)
        println("Default quantity: " + chickpeaNd.quantity + chickpeaNd.qtyUnitAbbr)
        println("mls quantity: " + millilitresNd.quantity + millilitresNd.qtyUnitAbbr)
        println("backConverted quantity: " + backConverted.quantity + backConverted.qtyUnitAbbr)
    }

    @Test
    fun testDensity2() {
        val chickPea100mL = NutritionCalculations.rescale(chickpeaNd, 100.0, Units.MILLILITRES)
        val water100mL = NutritionCalculations.rescale(waterNd, 100.0, Units.MILLILITRES)
        val combined = NutritionCalculations.sum(listOf(chickPea100mL, water100mL))
        Assertions.assertEquals(Units.GRAMS, combined.qtyUnit)
        Assertions.assertEquals(100 + 100 * chickpeaNd.density!!, combined.quantity)
        Assertions.assertTrue(combined.hasCompleteData(NutritionDataTable.QUANTITY))
    }

}