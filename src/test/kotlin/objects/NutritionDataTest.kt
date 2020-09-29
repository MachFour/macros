package objects

import com.machfour.macros.core.*
import com.machfour.macros.core.Schema.FoodPortionTable.Companion.FOOD_ID
import com.machfour.macros.core.Schema.FoodPortionTable.Companion.MEAL_ID
import com.machfour.macros.core.Schema.FoodPortionTable.Companion.QUANTITY
import com.machfour.macros.core.Schema.FoodPortionTable.Companion.QUANTITY_UNIT
import com.machfour.macros.objects.FoodPortion
import com.machfour.macros.objects.NutritionCalculations
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.QtyUnits
import com.machfour.macros.sample.ExampleFood
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class NutritionDataTest {
    companion object {

        private val f = ExampleFood.food2
        private val nd1: NutritionData // grams
        private val nd2: NutritionData // ml
        private val nd3: NutritionData // mg

        init {
            val fpBuilder = MacrosBuilder(FoodPortion.table)
            fpBuilder.setField(FOOD_ID, f.id)
            fpBuilder.setField(MEAL_ID, MacrosEntity.NO_ID)
            fpBuilder.setField(QUANTITY, 100.0)
            fpBuilder.setField(QUANTITY_UNIT, QtyUnits.GRAMS.abbr)
            val fp1 = fpBuilder.build()
            fpBuilder.setField(QUANTITY_UNIT, QtyUnits.MILLILITRES.abbr)
            val fp2 = fpBuilder.build()
            fpBuilder.setField(QUANTITY, 100000.0)
            fpBuilder.setField(QUANTITY_UNIT, QtyUnits.MILLIGRAMS.abbr)
            val fp3 = fpBuilder.build()


            fp1.initFood(f)
            fp2.initFood(f)
            fp3.initFood(f)

            nd1 = fp1.nutritionData
            nd2 = fp2.nutritionData
            nd3 = fp3.nutritionData
        }
    }

    @Test
    fun testScaling() {
        assertNotNull(f.getNutritionData().density)
        val density = f.getNutritionData().density!!
        val fat = f.getNutritionData().getData(Schema.NutritionDataTable.FAT)!!
        val fat1 = nd1.getData(Schema.NutritionDataTable.FAT)!!
        val fat2 = nd2.getData(Schema.NutritionDataTable.FAT)!!

        assertEquals(density, 0.92)
        assertEquals(fat, fat2)
        assertEquals(fat / density, fat1)
    }

    @Test
    fun testScaling2() {
        assertNotNull(f.getNutritionData().density)
        val fat1 = nd1.getData(Schema.NutritionDataTable.FAT)!!
        val fat3 = nd3.getData(Schema.NutritionDataTable.FAT)!!

        assertEquals(fat1, fat3)
    }

    // TODO test complete data propagation through sum and combining of data

    @Test
    fun testSum() {
        val sum = NutritionCalculations.sum(listOf(nd1, nd2), true)
        assertEquals(192.0, sum.getData(Schema.NutritionDataTable.FAT) as Double)
    }


}