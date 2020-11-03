package com.machfour.macros.sample

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Schema
import com.machfour.macros.core.Schema.FoodTable.Companion.BRAND
import com.machfour.macros.core.Schema.FoodTable.Companion.CATEGORY
import com.machfour.macros.core.Schema.FoodTable.Companion.FOOD_TYPE
import com.machfour.macros.core.Schema.FoodTable.Companion.INDEX_NAME
import com.machfour.macros.core.Schema.FoodTable.Companion.NAME
import com.machfour.macros.core.Schema.FoodTable.Companion.NOTES
import com.machfour.macros.core.Schema.FoodTable.Companion.NUTTAB_INDEX
import com.machfour.macros.objects.*

import com.machfour.macros.core.Schema.FoodTable.Companion.USDA_INDEX
import com.machfour.macros.core.Schema.FoodTable.Companion.VARIETY
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CALCIUM
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CARBOHYDRATE
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.DATA_SOURCE
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.DENSITY
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.FIBRE
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.IRON
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.KILOJOULES
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.MONOUNSATURATED_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.POLYUNSATURATED_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.QUANTITY
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.QUANTITY_UNIT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SATURATED_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SODIUM
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SUGAR
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.WATER

object ExampleFood {

    /*
     * Food with no nutrition data
     */
    val food1: Food = init1()

    /*
     * Food with nutrition data that has nonunit density
     */
    val food2: Food = init2()

    private fun init1(): Food {
        val data = ColumnData(Schema.FoodTable.instance)
        data.put(INDEX_NAME, "food1")
        data.put(BRAND, "Max's")
        data.put(VARIETY, "really good")
        data.put(NAME, "food")
        data.put(NOTES, "notes")
        data.put(CATEGORY, "dairy")
        data.put(FOOD_TYPE, FoodType.PRIMARY.niceName)
        data.put(USDA_INDEX, null)
        data.put(NUTTAB_INDEX, null)
        return Food.factory.construct(data, ObjectSource.IMPORT)
    }

    private fun init2(): Food {
        val fData = ColumnData(Food.table)
        val nData = ColumnData(NutritionData.table)
        fData.put(INDEX_NAME, "generic-oil")
        fData.put(NAME, "Generic Oil")
        fData.put(CATEGORY, "oils")
        fData.put(FOOD_TYPE, FoodType.PRIMARY.niceName)
        nData.put(KILOJOULES, 3400.0)
        nData.put(CARBOHYDRATE, 0.0)
        nData.put(FAT, 92.0)
        nData.put(SATURATED_FAT, 12.0)
        nData.put(SUGAR, 0.0)
        nData.put(SODIUM, 0.0)
        nData.put(POLYUNSATURATED_FAT, 23.0)
        nData.put(MONOUNSATURATED_FAT, 56.0)
        nData.put(WATER, 0.0)
        nData.put(FIBRE, 0.0)
        nData.put(CALCIUM, 34.0)
        nData.put(IRON, 10.0)
        nData.put(DATA_SOURCE, "Test")
        nData.put(DENSITY, 0.92)
        nData.put(QUANTITY, 100.0)
        nData.put(QUANTITY_UNIT, Units.MILLILITRES.abbr)
        val nd = NutritionData.table.construct(nData, ObjectSource.USER_NEW)
        val f = Food.factory.construct(fData, ObjectSource.IMPORT)
        f.setNutritionData(nd)
        return f
    }

}
