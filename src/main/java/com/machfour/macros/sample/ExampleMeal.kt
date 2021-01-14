package com.machfour.macros.sample

import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.schema.FoodPortionTable
import com.machfour.macros.core.schema.MealTable
import com.machfour.macros.core.schema.SchemaHelpers
import com.machfour.macros.objects.FoodPortion
import com.machfour.macros.objects.Meal
import com.machfour.macros.util.DateStamp

object ExampleMeal {
    private val food = ExampleFood.food2

    val foodPortion by lazy {
        initFoodPortion()
    }
    val meal: Meal by lazy {
        initMeal()
    }

    private fun initFoodPortion() : FoodPortion {
        val fp = MacrosBuilder(FoodPortion.table).run {
            setField(FoodPortionTable.MEAL_ID, MacrosEntity.NO_ID)
            setField(FoodPortionTable.FOOD_ID, food.id) // who knows what food this is haha
            setField(FoodPortionTable.QUANTITY, 100.0)
            setField(FoodPortionTable.QUANTITY_UNIT, "g")
            setField(FoodPortionTable.NOTES, "This is an example food portion")
            build()
        }

        fp.initFoodAndNd(food)
        return fp
    }

    private fun initMeal(): Meal {
        val meal = MacrosBuilder(Meal.table).run {
            setField(MealTable.DAY, DateStamp(2020, 10, 28))
            setField(MealTable.NAME, "Example meal")
            setField(MealTable.NOTES, "Notable notes")
            build()
        }
        
        meal.addFoodPortion(foodPortion)
        return meal
    }
}