package com.machfour.macros.sample

import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.core.FoodType
import com.machfour.macros.nutrients.Nutrients
import com.machfour.macros.units.Units
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.FoodTable.BRAND
import com.machfour.macros.orm.schema.FoodTable.CATEGORY
import com.machfour.macros.orm.schema.FoodTable.EXTRA_DESC
import com.machfour.macros.orm.schema.FoodTable.FOOD_TYPE
import com.machfour.macros.orm.schema.FoodTable.INDEX_NAME
import com.machfour.macros.orm.schema.FoodTable.NAME
import com.machfour.macros.orm.schema.FoodTable.NOTES
import com.machfour.macros.orm.schema.FoodTable.NUTTAB_INDEX
import com.machfour.macros.orm.schema.FoodTable.USDA_INDEX
import com.machfour.macros.orm.schema.FoodTable.VARIETY
import com.machfour.macros.sql.RowData

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
        val data = RowData(foodTable)
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
        val data = RowData(foodTable)
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
            , FoodNutrientValue.makeComputedValue(40.0, Nutrients.PROTEIN, Units.GRAMS)
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
