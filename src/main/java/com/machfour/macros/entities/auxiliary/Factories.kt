package com.machfour.macros.entities.auxiliary

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.Factory
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.schema.FoodTable
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit

// Contains factories for the different objects
// They're here because putting them in the same file as the object causes static initialisation order issues

object Factories {


    val food: Factory<Food> = Factory { data, objectSource ->
        // index name completion
        if (data[FoodTable.INDEX_NAME] == null) {
            val name = data[FoodTable.NAME]!!
            val brand = data[FoodTable.BRAND]
            val variety = data[FoodTable.VARIETY]
            val extraDesc = data[FoodTable.EXTRA_DESC]
            data.put(FoodTable.INDEX_NAME, Food.indexNamePrototype(name, brand, variety, extraDesc))
        }
        when (FoodType.fromString(data[FoodTable.FOOD_TYPE]!!)) {
            FoodType.COMPOSITE -> CompositeFood(data, objectSource)
            else -> Food(data, objectSource)
        }
    }

    private fun <M> defaultFactory(constructor: (ColumnData<M>, ObjectSource) -> M) : Factory<M> {
        return Factory { dataMap, objectSource -> constructor(dataMap, objectSource) }
    }

    val serving = defaultFactory(::Serving)

    val meal = defaultFactory(::Meal)

    val foodPortion = defaultFactory(::FoodPortion)

    val ingredient = defaultFactory(::Ingredient)

    val foodNutrientValue = defaultFactory(::FoodNutrientValue)

    val mealNutrientGoalValue = defaultFactory(::MealNutrientGoalValue)

    val dayNutrientGoalValue = defaultFactory(::DayNutrientGoalValue)

    val nutrient = defaultFactory(::Nutrient)

    val unit = defaultFactory(::Unit)

}