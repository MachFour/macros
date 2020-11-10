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
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Units

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
        fData.put(INDEX_NAME, "generic-oil")
        fData.put(NAME, "Generic Oil")
        fData.put(CATEGORY, "oils")
        fData.put(FOOD_TYPE, FoodType.PRIMARY.niceName)
        val f = Food.factory.construct(fData, ObjectSource.IMPORT)

        val nutritionData = listOf(
              NutrientValue.makeObject(3400.0, Nutrients.ENERGY, Units.KILOJOULES, food = f)
            , NutrientValue.makeObject(20.0, Nutrients.CARBOHYDRATE, Units.GRAMS, food = f)
            , NutrientValue.makeObject(90.0, Nutrients.FAT, Units.GRAMS, food = f)
            , NutrientValue.makeObject(12.0, Nutrients.SATURATED_FAT, Units.GRAMS, food = f)
            , NutrientValue.makeObject(50.0, Nutrients.SUGAR, Units.GRAMS, food = f)
            , NutrientValue.makeObject(20.0, Nutrients.POLYUNSATURATED_FAT, Units.GRAMS, food = f)
            , NutrientValue.makeObject(10.0, Nutrients.MONOUNSATURATED_FAT, Units.GRAMS, food = f)
            , NutrientValue.makeObject(100.0, Nutrients.WATER, Units.GRAMS, food = f)
            , NutrientValue.makeObject(2.0, Nutrients.FIBRE, Units.GRAMS, food = f)
            , NutrientValue.makeObject(1000.0, Nutrients.SODIUM, Units.MILLIGRAMS, food = f)
            , NutrientValue.makeObject(200.0, Nutrients.CALCIUM, Units.MILLIGRAMS, food = f)
            , NutrientValue.makeObject(40.0, Nutrients.IRON, Units.MILLIGRAMS, food = f)
            , NutrientValue.makeObject(1000.0, Nutrients.QUANTITY, Units.MILLILITRES, food = f)
        )

        for (nv in nutritionData) {
            f.addNutrientValue(nv)
        }

        return f
    }

}
