package com.machfour.macros.entities.auxiliary

import com.machfour.macros.core.Factory
import com.machfour.macros.core.FoodType
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.foodname.indexNamePrototype
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.RowData

// Contains factories for the different objects
// They're here because putting them in the same file as the object causes static initialisation order issues


// TODO make factories lazy inside each class and make object constructors private

object Factories {

    val food: Factory<Food> = Factory { data, objectSource ->
        // index name completion
        if (data[FoodTable.INDEX_NAME] == null) {
            val name = data[FoodTable.NAME]?: "food"
            val brand = data[FoodTable.BRAND]
            val variety = data[FoodTable.VARIETY]
            val extraDesc = data[FoodTable.EXTRA_DESC]
            data.put(FoodTable.INDEX_NAME, indexNamePrototype(name, brand, variety, extraDesc))
        }

        data.makeImmutable()

        when (FoodType.fromString(data[FoodTable.FOOD_TYPE]!!)) {
            FoodType.COMPOSITE -> CompositeFood(data, objectSource)
            else -> Food(data, objectSource)
        }
    }

    private fun <M> defaultFactory(constructor: (RowData<M>, ObjectSource) -> M) : Factory<M> {
        return Factory { data, objectSource ->
            data.makeImmutable()
            constructor(data, objectSource)
        }
    }

    val attributeMapping = defaultFactory(::AttrMapping)

    val foodCategory = defaultFactory(::FoodCategory)

    val serving = defaultFactory(::Serving)

    val meal = defaultFactory(::Meal)

    val foodPortion = defaultFactory(::FoodPortion)

    val ingredient = defaultFactory(::Ingredient)

    val foodNutrientValue = defaultFactory(::FoodNutrientValue)

    val nutrientGoal = defaultFactory(::NutrientGoal)

    val nutrientGoalDayMapping = defaultFactory(::NutrientGoalDayMapping)

    val nutrientGoalValue = defaultFactory(::NutrientGoalValue)

    val nutrient = defaultFactory(::Nutrient)

    val unit = defaultFactory(::Unit)

}