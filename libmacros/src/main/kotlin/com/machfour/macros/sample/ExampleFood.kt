package com.machfour.macros.sample

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.FoodType
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.id
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.nutrients.*
import com.machfour.macros.schema.FoodCategoryTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.FoodTable.BRAND
import com.machfour.macros.schema.FoodTable.CATEGORY
import com.machfour.macros.schema.FoodTable.DENSITY
import com.machfour.macros.schema.FoodTable.EXTRA_DESC
import com.machfour.macros.schema.FoodTable.FOOD_TYPE
import com.machfour.macros.schema.FoodTable.ID
import com.machfour.macros.schema.FoodTable.INDEX_NAME
import com.machfour.macros.schema.FoodTable.NAME
import com.machfour.macros.schema.FoodTable.NOTES
import com.machfour.macros.schema.FoodTable.NUTTAB_INDEX
import com.machfour.macros.schema.FoodTable.USDA_INDEX
import com.machfour.macros.schema.FoodTable.VARIETY
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.*

// Food with no nutrition data
val exampleFood1: Food by lazy {
    initExampleFood1()
}

// Food with nutrition data that has non-unit density
val exampleFood2: Food by lazy {
    initExampleFood2()
}

val exampleFoodCategory by lazy {
    makeExampleFoodCategory("uncategorised")
}

fun makeExampleFoodCategory(name: String): FoodCategory {
    return RowData(FoodCategoryTable).run {
        put(FoodCategoryTable.NAME, name)
        FoodCategory.factory.construct(this, ObjectSource.TEST)
    }
}

private fun initExampleFood1(): Food {
    val data = RowData(FoodTable)
    data.put(ID, EntityId(1L))
    data.put(INDEX_NAME, "food1")
    data.put(BRAND, "Max's")
    data.put(VARIETY, "really good with a really long variety name just to see what happens")
    data.put(NAME, "food")
    data.put(NOTES, "notes")
    data.put(CATEGORY, "uncategorised")
    data.put(FOOD_TYPE, FoodType.PRIMARY.niceName)
    data.put(USDA_INDEX, null)
    data.put(NUTTAB_INDEX, null)
    val f = Food.factory.construct(data, ObjectSource.TEST)

    f.setFoodCategory(exampleFoodCategory)

    val nutritionData = listOf(
        FoodNutrientValue.makeComputedValue(200.0, ENERGY, KILOJOULES),
        FoodNutrientValue.makeComputedValue(4.0, PROTEIN, GRAMS),
        FoodNutrientValue.makeComputedValue(12.0, CARBOHYDRATE, GRAMS),
        FoodNutrientValue.makeComputedValue(9.0, FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(1.0, SATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(5.0, SUGAR, GRAMS),
        FoodNutrientValue.makeComputedValue(5.0, POLYUNSATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(2.0, MONOUNSATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(10.0, WATER, MILLILITRES),
        FoodNutrientValue.makeComputedValue(10.0, FIBRE, GRAMS),
        FoodNutrientValue.makeComputedValue(500.0, SODIUM, MILLIGRAMS),
        FoodNutrientValue.makeComputedValue(20.0, CALCIUM, MILLIGRAMS),
        FoodNutrientValue.makeComputedValue(4.0, IRON, MILLIGRAMS),
        FoodNutrientValue.makeComputedValue(100.0, QUANTITY, GRAMS),
    )

    for (nv in nutritionData) {
        f.addNutrientValue(nv)
    }

    return f
}

private fun initExampleFood2(): Food {
    val data = RowData(FoodTable)
    data.put(ID, 2.id)
    data.put(INDEX_NAME, "generic-oil")
    data.put(VARIETY, "Super oily")
    data.put(EXTRA_DESC, "in a bottle")
    data.put(NAME, "Generic Oil")
    data.put(BRAND, "Max's")
    data.put(NOTES, "it's still organic though")
    data.put(CATEGORY, "oils")
    data.put(DENSITY, 0.92)
    data.put(FOOD_TYPE, FoodType.PRIMARY.niceName)
    val f = Food.factory.construct(data, ObjectSource.TEST)

    val foodCategory = makeExampleFoodCategory("oils")
    f.setFoodCategory(foodCategory)

    val nutritionData = listOf(
        FoodNutrientValue.makeComputedValue(1000.0, ENERGY, CALORIES),
        FoodNutrientValue.makeComputedValue(40.0, PROTEIN, GRAMS),
        FoodNutrientValue.makeComputedValue(20.0, CARBOHYDRATE, GRAMS),
        FoodNutrientValue.makeComputedValue(92.0, FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(12.0, SATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(50.0, SUGAR, GRAMS),
        FoodNutrientValue.makeComputedValue(20.0, POLYUNSATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(10.0, MONOUNSATURATED_FAT, GRAMS),
        FoodNutrientValue.makeComputedValue(100.0, WATER, GRAMS),
        FoodNutrientValue.makeComputedValue(2.0, FIBRE, GRAMS),
        FoodNutrientValue.makeComputedValue(1000.0, SODIUM, MILLIGRAMS),
        FoodNutrientValue.makeComputedValue(200.0, CALCIUM, MILLIGRAMS),
        FoodNutrientValue.makeComputedValue(40.0, IRON, MILLIGRAMS),
        FoodNutrientValue.makeComputedValue(100.0, QUANTITY, MILLILITRES),
    )

    for (nv in nutritionData) {
        f.addNutrientValue(nv)
    }

    return f
}