package com.machfour.macros.objects

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.nutrients.FAT
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.sample.exampleFood2
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.FoodPortionTable.FOOD_ID
import com.machfour.macros.schema.FoodPortionTable.MEAL_ID
import com.machfour.macros.schema.FoodPortionTable.QUANTITY
import com.machfour.macros.schema.FoodPortionTable.QUANTITY_UNIT
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLIGRAMS
import com.machfour.macros.units.MILLILITRES
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NutrientDataTest {
    companion object {

        private val f = exampleFood2
        private val nd1: FoodNutrientData // grams
        private val nd2: FoodNutrientData // ml
        private val nd3: FoodNutrientData // mg

        init {
            val fp1: FoodPortion
            val fp2: FoodPortion
            val fp3: FoodPortion
            val factory = FoodPortion.factory
            with (RowData(FoodPortionTable)) {
                put(FOOD_ID, f.id)
                put(MEAL_ID, MacrosEntity.NO_ID)
                put(QUANTITY, 100.0)
                put(QUANTITY_UNIT, GRAMS.abbr)
                fp1 = factory.construct(this, ObjectSource.TEST)
                put(QUANTITY_UNIT, MILLILITRES.abbr)
                fp2 = factory.construct(this, ObjectSource.TEST)
                put(QUANTITY, 100000.0)
                put(QUANTITY_UNIT, MILLIGRAMS.abbr)
                fp3 = factory.construct(this, ObjectSource.TEST)
            }

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
        val fat = f.nutrientData.amountOf(FAT)!!
        val fat1 = nd1.amountOf(FAT)!!
        val fat2 = nd2.amountOf(FAT)!!

        assertEquals(density, 0.92)
        assertEquals(fat, fat2)
        assertEquals(fat / density, fat1)
    }

    @Test
    fun testScaling2() {
        assertNotNull(f.density)
        val fat1 = nd1.amountOf(FAT)!!
        val fat3 = nd3.amountOf(FAT)!!

        assertEquals(fat1, fat3)
    }

    // TODO test complete data propagation through sum and combining of data

    @Test
    fun testSum() {
        assertEquals(100.0, nd1.amountOf(FAT))
        val sum = FoodNutrientData.sum(listOf(nd1, nd2))
        assertEquals(192.0, sum.amountOf(FAT))
    }


}