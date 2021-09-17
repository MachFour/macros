package com.machfour.macros.sample

import com.machfour.macros.core.FoodType
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.nutrients.*
import com.machfour.macros.schema.FoodTable.BRAND
import com.machfour.macros.schema.FoodTable.CATEGORY
import com.machfour.macros.schema.FoodTable.EXTRA_DESC
import com.machfour.macros.schema.FoodTable.FOOD_TYPE
import com.machfour.macros.schema.FoodTable.INDEX_NAME
import com.machfour.macros.schema.FoodTable.NAME
import com.machfour.macros.schema.FoodTable.NOTES
import com.machfour.macros.schema.FoodTable.NUTTAB_INDEX
import com.machfour.macros.schema.FoodTable.USDA_INDEX
import com.machfour.macros.schema.FoodTable.VARIETY
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.CALORIES
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLIGRAMS
import com.machfour.macros.units.MILLILITRES

// Food with no nutrition data
val exampleFood1: Food by lazy {
    init1()
}

// Food with nutrition data that has nonunit density
val exampleFood2: Food by lazy {
    init2()
}

private fun init1(): Food {
    val data = RowData(Food.table)
    data.put(INDEX_NAME, "food1")
    data.put(BRAND, "Max's")
    data.put(VARIETY, "really good with a really long variety name just to see what happens")
    data.put(NAME, "food")
    data.put(NOTES, "notes")
    data.put(CATEGORY, "dairy")
    data.put(FOOD_TYPE, FoodType.PRIMARY.niceName)
    data.put(USDA_INDEX, null)
    data.put(NUTTAB_INDEX, null)
    return Food.factory.construct(data, ObjectSource.IMPORT)
}

private fun init2(): Food {
    val data = RowData(Food.table)
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
          FoodNutrientValue.makeComputedValue(1000.0, ENERGY, CALORIES)
        , FoodNutrientValue.makeComputedValue(40.0, PROTEIN, GRAMS)
        , FoodNutrientValue.makeComputedValue(20.0, CARBOHYDRATE, GRAMS)
        , FoodNutrientValue.makeComputedValue(90.0, FAT, GRAMS)
        , FoodNutrientValue.makeComputedValue(12.0, SATURATED_FAT, GRAMS)
        , FoodNutrientValue.makeComputedValue(50.0, SUGAR, GRAMS)
        , FoodNutrientValue.makeComputedValue(20.0, POLYUNSATURATED_FAT, GRAMS)
        , FoodNutrientValue.makeComputedValue(10.0, MONOUNSATURATED_FAT, GRAMS)
        , FoodNutrientValue.makeComputedValue(100.0, WATER, GRAMS)
        , FoodNutrientValue.makeComputedValue(2.0, FIBRE, GRAMS)
        , FoodNutrientValue.makeComputedValue(1000.0, SODIUM, MILLIGRAMS)
        , FoodNutrientValue.makeComputedValue(200.0, CALCIUM, MILLIGRAMS)
        , FoodNutrientValue.makeComputedValue(40.0, IRON, MILLIGRAMS)
        , FoodNutrientValue.makeComputedValue(100.0, QUANTITY, MILLILITRES)
    )

    for (nv in nutritionData) {
        f.addNutrientValue(nv)
    }

    return f
}