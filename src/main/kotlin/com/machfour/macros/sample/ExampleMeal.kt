package com.machfour.macros.sample

import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.MealTable
import com.machfour.macros.util.DateStamp

val exampleFoodPortion by lazy { initFoodPortion() }

val exampleMeal: Meal by lazy { initMeal(1) }
val exampleMeal2: Meal by lazy { initMeal(2) }
val exampleMeal3: Meal by lazy { initMeal(3) }
val exampleMeal4: Meal by lazy { initMeal(4) }
val exampleMeal5: Meal by lazy { initMeal(5) }

private fun initFoodPortion() : FoodPortion {
    val food = exampleFood2
    val fp = MacrosBuilder(FoodPortion.table).run {
        setField(FoodPortionTable.MEAL_ID, MacrosEntity.NO_ID)
        setField(FoodPortionTable.FOOD_ID, food.id) // who knows what food this is haha
        setField(FoodPortionTable.QUANTITY, 50.0)
        setField(FoodPortionTable.QUANTITY_UNIT, "g")
        setField(FoodPortionTable.NOTES, "This is an example food portion")
        build()
    }

    fp.initFoodAndNd(food)
    return fp
}

private fun initMeal(n: Int): Meal {
    val meal = MacrosBuilder(Meal.table).run {
        setField(MealTable.ID, n.toLong())
        setField(MealTable.DAY, DateStamp(2020, 10, 28))
        setField(MealTable.NAME, "Example meal $n")
        setField(MealTable.NOTES, "Notable notes")
        build(ObjectSource.TEST)
    }

    repeat(n) {
        meal.addFoodPortion(exampleFoodPortion)
    }
    return meal
}