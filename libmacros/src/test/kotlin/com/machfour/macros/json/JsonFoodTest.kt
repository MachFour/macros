package com.machfour.macros.json

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.*
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.sql.rowdata.foodToRowData
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.KILOJOULES
import com.machfour.macros.units.MILLIGRAMS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JsonFoodTest {

    @Test
    fun toRowData() {
        val actual = foodToRowData(apple)
        assertEquals(MacrosEntity.NO_ID, actual[FoodTable.ID])
        assertEquals(0, actual[FoodTable.CREATE_TIME])
        assertEquals(0, actual[FoodTable.MODIFY_TIME])
        assertEquals("apple", actual[FoodTable.NAME])
        assertEquals(null, actual[FoodTable.BRAND])
        assertEquals("pink lady", actual[FoodTable.VARIETY])
        assertEquals(null, actual[FoodTable.EXTRA_DESC])
        assertEquals("apple-pink", actual[FoodTable.INDEX_NAME])
        assertEquals(null, actual[FoodTable.NOTES])
        assertEquals("06D10433", actual[FoodTable.NUTTAB_INDEX])
        assertEquals("NUTTAB", actual[FoodTable.DATA_SOURCE])
        assertEquals(null, actual[FoodTable.DATA_NOTES])
        assertEquals(null, actual[FoodTable.USDA_INDEX])
        assertEquals(null, actual[FoodTable.DENSITY])
        assertEquals(0, actual[FoodTable.SEARCH_RELEVANCE])
    }

    @Test
    fun getServingData() {
        val actual = apple.getServingData()
        assertEquals(2, actual.size)

        val actual0 = actual[0]
        assertEquals(MacrosEntity.NO_ID, actual0[ServingTable.ID])
        assertEquals(0, actual0[ServingTable.CREATE_TIME])
        assertEquals(0, actual0[ServingTable.MODIFY_TIME])
        assertEquals("small", actual0[ServingTable.NAME])
        assertEquals(80.0, actual0[ServingTable.QUANTITY])
        assertEquals("g", actual0[ServingTable.QUANTITY_UNIT])
        assertEquals(false, actual0[ServingTable.IS_DEFAULT])
        assertEquals(MacrosEntity.NO_ID, actual0[ServingTable.FOOD_ID])
        assertEquals("about the size of a mandarin", actual0[ServingTable.NOTES])

        val actual1 = actual[1]
        assertEquals(MacrosEntity.NO_ID, actual1[ServingTable.ID])
        assertEquals(0, actual1[ServingTable.CREATE_TIME])
        assertEquals(0, actual1[ServingTable.MODIFY_TIME])
        assertEquals("large", actual1[ServingTable.NAME])
        assertEquals(120.0, actual1[ServingTable.QUANTITY])
        assertEquals("g", actual1[ServingTable.QUANTITY_UNIT])
        assertEquals(false, actual1[ServingTable.IS_DEFAULT])
        assertEquals(MacrosEntity.NO_ID, actual1[ServingTable.FOOD_ID])
        assertEquals("about the size of a tennis ball", actual1[ServingTable.NOTES])
    }

    private fun checkQuantity(
        food: JsonFood,
        amount: Double,
        unit: Unit
    ) {
    }

    private fun checkNutrientValue(
        nutrientValues: Map<INutrient, RowData<FoodNutrientValue>>,
        nutrient: INutrient,
        amount: Double,
        unit: Unit
    ) {
        val data = nutrientValues[nutrient]
        assertNotNull(data)
        assertEquals(MacrosEntity.NO_ID, data[FoodNutrientValueTable.ID])
        assertEquals(0, data[FoodNutrientValueTable.CREATE_TIME])
        assertEquals(0, data[FoodNutrientValueTable.MODIFY_TIME])
        assertEquals(0, data[FoodNutrientValueTable.CONSTRAINT_SPEC])
        assertEquals(MacrosEntity.NO_ID, data[FoodNutrientValueTable.FOOD_ID])
        assertEquals(nutrient.id, data[FoodNutrientValueTable.NUTRIENT_ID])
        assertEquals(unit.id, data[FoodNutrientValueTable.UNIT_ID])
        assertEquals(amount, data[FoodNutrientValueTable.VALUE])
    }

    @Test
    fun getNutrientValueData() {
        val data = apple.getNutrientValueData()
        checkNutrientValue(data, QUANTITY,100.0, GRAMS)
        checkNutrientValue(data, ENERGY, 247.0, KILOJOULES)
        checkNutrientValue(data, PROTEIN, 0.3, GRAMS)
        checkNutrientValue(data, FAT, 0.4, GRAMS)
        checkNutrientValue(data, SATURATED_FAT, 0.0, GRAMS)
        checkNutrientValue(data, CARBOHYDRATE, 12.4, GRAMS)
        checkNutrientValue(data, SUGAR, 11.9, GRAMS)
        checkNutrientValue(data, FIBRE, 2.4, GRAMS)
        checkNutrientValue(data, SODIUM, 0.0, MILLIGRAMS)

    }
}