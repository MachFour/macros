package objects

import com.machfour.macros.core.*
import com.machfour.macros.core.Schema.FoodQuantityTable.Companion.FOOD_ID
import com.machfour.macros.core.Schema.FoodQuantityTable.Companion.MEAL_ID
import com.machfour.macros.core.Schema.FoodQuantityTable.Companion.QUANTITY
import com.machfour.macros.core.Schema.FoodQuantityTable.Companion.QUANTITY_UNIT
import com.machfour.macros.objects.*
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Units
import com.machfour.macros.sample.ExampleFood
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class NutrientDataTest {
    companion object {

        private val f = ExampleFood.food2
        private val nd1: NutrientData // grams
        private val nd2: NutrientData // ml
        private val nd3: NutrientData // mg

        init {
            val fpBuilder = MacrosBuilder(FoodQuantity.table)
            fpBuilder.setField(FOOD_ID, f.id)
            fpBuilder.setField(MEAL_ID, MacrosEntity.NO_ID)
            fpBuilder.setField(QUANTITY, 100.0)
            fpBuilder.setField(QUANTITY_UNIT, Units.GRAMS.abbr)
            val fp1 = fpBuilder.build() as FoodPortion
            fpBuilder.setField(QUANTITY_UNIT, Units.MILLILITRES.abbr)
            val fp2 = fpBuilder.build() as FoodPortion
            fpBuilder.setField(QUANTITY, 100000.0)
            fpBuilder.setField(QUANTITY_UNIT, Units.MILLIGRAMS.abbr)
            val fp3 = fpBuilder.build() as FoodPortion


            fp1.initFoodAndNd(f)
            fp2.initFoodAndNd(f)
            fp3.initFoodAndNd(f)

            nd1 = fp1.nutrientData
            nd2 = fp2.nutrientData
            nd3 = fp3.nutrientData
        }
    }

    @Test
    fun testScaling() {
        assertNotNull(f.density)
        val density = f.density!!
        val fat = f.nutrientData.amountOf(Nutrients.FAT)!!
        val fat1 = nd1.amountOf(Nutrients.FAT)!!
        val fat2 = nd2.amountOf(Nutrients.FAT)!!

        assertEquals(density, 0.92)
        assertEquals(fat, fat2)
        assertEquals(fat / density, fat1)
    }

    @Test
    fun testScaling2() {
        assertNotNull(f.density)
        val fat1 = nd1.amountOf(Nutrients.FAT)!!
        val fat3 = nd3.amountOf(Nutrients.FAT)!!

        assertEquals(fat1, fat3)
    }

    // TODO test complete data propagation through sum and combining of data

    @Test
    fun testSum() {
        val sum = NutrientData.sum(listOf(nd1, nd2))
        assertEquals(192.0, sum.amountOf(Nutrients.FAT) as Double)
    }


}