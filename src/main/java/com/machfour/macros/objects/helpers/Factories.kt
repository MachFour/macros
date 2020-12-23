package com.machfour.macros.objects.helpers

import com.machfour.macros.core.Factory
import com.machfour.macros.core.Schema
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit

// Contains factories for the different objects
// They're here because putting them in the same file as the object causes static initialisation order issues

object Factories {

    val food: Factory<Food> = Factory { dataMap, objectSource ->
        when (FoodType.fromString(dataMap[Schema.FoodTable.FOOD_TYPE]!!)) {
            FoodType.COMPOSITE -> CompositeFood(dataMap, objectSource)
            else -> Food(dataMap, objectSource)
        }
    }

    val serving: Factory<Serving> = Factory { dataMap, objectSource -> Serving(dataMap, objectSource) }

    val meal: Factory<Meal> = Factory { dataMap, objectSource -> Meal(dataMap, objectSource) }

    val foodQuantity : Factory<FoodQuantity> = Factory {
        dataMap, objectSource ->
        val parentFoodId = dataMap[Schema.FoodQuantityTable.PARENT_FOOD_ID]
        val mealId = dataMap[Schema.FoodQuantityTable.MEAL_ID]
        require((parentFoodId != null) xor (mealId != null)) { "Exactly one of mealId and parentFoodId must be defined" }
        when {
            parentFoodId != null -> Ingredient(dataMap, objectSource)
            mealId != null -> FoodPortion(dataMap, objectSource)
            else -> throw IllegalArgumentException("Both meal id and parent food id are null")
        }
    }

    val nutrientValue: Factory<NutrientValue> = Factory { dataMap, objectSource -> NutrientValue(dataMap, objectSource) }

    val nutrient: Factory<Nutrient> = Factory { dataMap, objectSource -> Nutrient(dataMap, objectSource) }

    val unit: Factory<Unit> = Factory { dataMap, objectSource -> Unit(dataMap, objectSource) }

}