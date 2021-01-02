package com.machfour.macros.sample

import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.Schema
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
            setField(Schema.FoodPortionTable.MEAL_ID, MacrosEntity.NO_ID)
            setField(Schema.FoodPortionTable.FOOD_ID, food.id) // who knows what food this is haha
            setField(Schema.FoodPortionTable.QUANTITY, 100.0)
            setField(Schema.FoodPortionTable.QUANTITY_UNIT, "g")
            setField(Schema.FoodPortionTable.NOTES, "This is an example food portion")
            build()
        }

        fp.initFoodAndNd(food)
        return fp
    }

    private fun initMeal(): Meal {
        val meal = MacrosBuilder(Meal.table).run {
            setField(Schema.MealTable.DAY, DateStamp(2020, 10, 28))
            setField(Schema.MealTable.NAME, "Example meal")
            setField(Schema.MealTable.NOTES, "Notable notes")
            build()
        }
        
        meal.addFoodPortion(foodPortion)
        return meal
    }
}