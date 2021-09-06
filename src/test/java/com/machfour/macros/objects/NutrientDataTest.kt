package com.machfour.macros.objects

import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.Nutrients
import com.machfour.macros.orm.schema.FoodPortionTable.FOOD_ID
import com.machfour.macros.orm.schema.FoodPortionTable.MEAL_ID
import com.machfour.macros.orm.schema.FoodPortionTable.QUANTITY
import com.machfour.macros.orm.schema.FoodPortionTable.QUANTITY_UNIT
import com.machfour.macros.sample.exampleFood2
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLIGRAMS
import com.machfour.macros.units.MILLILITRES
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class NutrientDataTest {
    companion object {

        private val f = exampleFood2
        private val nd1: FoodNutrientData // grams
        private val nd2: FoodNutrientData // ml
        private val nd3: FoodNutrientData // mg

        init {
            val fpBuilder = MacrosBuilder(FoodPortion.table)
            fpBuilder.setField(FOOD_ID, f.id)
            fpBuilder.setField(MEAL_ID, MacrosEntity.NO_ID)
            fpBuilder.setField(QUANTITY, 100.0)
            fpBuilder.setField(QUANTITY_UNIT, GRAMS.abbr)
            val fp1 = fpBuilder.build()
            fpBuilder.setField(QUANTITY_UNIT, MILLILITRES.abbr)
            val fp2 = fpBuilder.build()
            fpBuilder.setField(QUANTITY, 100000.0)
            fpBuilder.setField(QUANTITY_UNIT, MILLIGRAMS.abbr)
            val fp3 = fpBuilder.build()


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
        val sum = FoodNutrientData.sum(listOf(nd1, nd2))
        assertEquals(192.0, sum.amountOf(Nutrients.FAT) as Double)
    }


}