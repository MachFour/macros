package com.machfour.macros.nutrients

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.sample.exampleFood2
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLIGRAMS
import com.machfour.macros.units.MILLILITRES
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NutrientDataTest {
    companion object {

        private val f = exampleFood2
        private val nd1: NutrientData<FoodNutrientValue> // grams
        private val nd2: NutrientData<FoodNutrientValue> // ml
        private val nd3: NutrientData<FoodNutrientValue> // mg

        init {
            val data = RowData(FoodPortionTable).apply {
                put(FoodPortionTable.FOOD_ID, f.id)
                put(FoodPortionTable.MEAL_ID, MacrosEntity.NO_ID)
                put(FoodPortionTable.QUANTITY, 100.0)
                put(FoodPortionTable.QUANTITY_UNIT, GRAMS.abbr)
            }
            val fp1 = FoodPortion.factory.construct(data.copy(), ObjectSource.TEST)
            data.put(FoodPortionTable.QUANTITY_UNIT, MILLILITRES.abbr)
            val fp2 = FoodPortion.factory.construct(data.copy(), ObjectSource.TEST)
            data.put(FoodPortionTable.QUANTITY, 100000.0)
            data.put(FoodPortionTable.QUANTITY_UNIT, MILLIGRAMS.abbr)
            val fp3 = FoodPortion.factory.construct(data.copy(), ObjectSource.TEST)

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
        val density = assertNotNull(f.density)
        val fat = f.nutrientData.amountOf(FAT, defaultValue = 0.0)
        val fat1 = nd1.amountOf(FAT, defaultValue = 0.0)
        val fat2 = nd2.amountOf(FAT, defaultValue = 0.0)

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