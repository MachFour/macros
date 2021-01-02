package com.machfour.macros.objects.helpers

import com.machfour.macros.core.ColumnData
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

    val foodPortion: Factory<FoodPortion> = Factory { dataMap, objectSource -> FoodPortion(dataMap, objectSource) }

    val ingredient: Factory<Ingredient> = Factory { dataMap, objectSource -> Ingredient(dataMap, objectSource) }

    val nutrientValue: Factory<NutrientValue> = Factory { dataMap, objectSource -> NutrientValue(dataMap, objectSource) }

    val nutrient: Factory<Nutrient> = Factory { dataMap, objectSource -> Nutrient(dataMap, objectSource) }

    val unit: Factory<Unit> = Factory { dataMap, objectSource -> Unit(dataMap, objectSource) }

}