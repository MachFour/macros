package com.machfour.macros.objects.helpers

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.Factory
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.schema.FoodTable
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit

// Contains factories for the different objects
// They're here because putting them in the same file as the object causes static initialisation order issues

object Factories {

    val food: Factory<Food> = Factory { dataMap, objectSource ->
        when (FoodType.fromString(dataMap[FoodTable.FOOD_TYPE]!!)) {
            FoodType.COMPOSITE -> CompositeFood(dataMap, objectSource)
            else -> Food(dataMap, objectSource)
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