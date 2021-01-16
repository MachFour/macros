package com.machfour.macros.sample

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.schema.FoodTable.Companion.BRAND
import com.machfour.macros.core.schema.FoodTable.Companion.CATEGORY
import com.machfour.macros.core.schema.FoodTable.Companion.EXTRA_DESC
import com.machfour.macros.core.schema.FoodTable.Companion.FOOD_TYPE
import com.machfour.macros.core.schema.FoodTable.Companion.INDEX_NAME
import com.machfour.macros.core.schema.FoodTable.Companion.NAME
import com.machfour.macros.core.schema.FoodTable.Companion.NOTES
import com.machfour.macros.core.schema.FoodTable.Companion.NUTTAB_INDEX
import com.machfour.macros.objects.*

import com.machfour.macros.core.schema.FoodTable.Companion.USDA_INDEX
import com.machfour.macros.core.schema.FoodTable.Companion.VARIETY
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Units

object ExampleFood {
    private val foodTable = Food.table


    /*
     * Food with no nutrition data
     */
    val food1: Food by lazy {
        init1()
    }

    /*
     * Food with nutrition data that has nonunit density
     */
    val food2: Food by lazy {
        init2()
    }

    private fun init1(): Food {
        val data = ColumnData(foodTable)
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
        val data = ColumnData(foodTable)
        data.put(INDEX_NAME, "generic-oil")
        data.put(VARIETY, "Super oily")
        data.put(EXTRA_DESC, "in a bottle")
        data.put(NAME, "Generic Oil")
        data.put(BRAND, "Max's")
        data.put(NOTES, "it's still organic though")
        data.put(CATEGORY, "oils")
        data.put(FOOD_TYPE, FoodType.PRIMARY.niceName)
        val f = Food.factory.construct(data, ObjectSource.IMPORT)

        val nutritionData = listOf(
              FoodNutrientValue.makeComputedValue(3400.0, Nutrients.ENERGY, Units.KILOJOULES)
            , FoodNutrientValue.makeComputedValue(20.0, Nutrients.CARBOHYDRATE, Units.GRAMS)
            , FoodNutrientValue.makeComputedValue(90.0, Nutrients.FAT, Units.GRAMS)
            , FoodNutrientValue.makeComputedValue(12.0, Nutrients.SATURATED_FAT, Units.GRAMS)
            , FoodNutrientValue.makeComputedValue(50.0, Nutrients.SUGAR, Units.GRAMS)
            , FoodNutrientValue.makeComputedValue(20.0, Nutrients.POLYUNSATURATED_FAT, Units.GRAMS)
            , FoodNutrientValue.makeComputedValue(10.0, Nutrients.MONOUNSATURATED_FAT, Units.GRAMS)
            , FoodNutrientValue.makeComputedValue(100.0, Nutrients.WATER, Units.GRAMS)
            , FoodNutrientValue.makeComputedValue(2.0, Nutrients.FIBRE, Units.GRAMS)
            , FoodNutrientValue.makeComputedValue(1000.0, Nutrients.SODIUM, Units.MILLIGRAMS)
            , FoodNutrientValue.makeComputedValue(200.0, Nutrients.CALCIUM, Units.MILLIGRAMS)
            , FoodNutrientValue.makeComputedValue(40.0, Nutrients.IRON, Units.MILLIGRAMS)
            , FoodNutrientValue.makeComputedValue(100.0, Nutrients.QUANTITY, Units.MILLILITRES)
        )

        for (nv in nutritionData) {
            f.addNutrientValue(nv)
        }

        return f
    }

}
