package com.machfour.macros.sample

import com.machfour.datestamp.DateStamp
import com.machfour.datestamp.makeDateStamp
import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.MealTable

val exampleFoodPortion by lazy { generateFp(exampleFood2, 50.0) }

val exampleMeals: List<Meal> by lazy {
    val day = makeDateStamp(2020, 10, 28)
    List(5) {
        val id = (it + 1).toLong()
        val fps = List(it + 1) { generateFp(exampleFood2, 50.0, mealId = id) }
        generateMeal(id, day, fps)
    }
}

fun generateFp(
    food: Food,
    quantity: Double,
    id: Long = MacrosEntity.NO_ID,
    mealId: Long = MacrosEntity.NO_ID,
) : FoodPortion {
    val fp = MacrosBuilder(FoodPortionTable).run {
        setField(FoodPortionTable.ID, id)
        setField(FoodPortionTable.MEAL_ID, mealId)
        setField(FoodPortionTable.FOOD_ID, food.id) // who knows what food this is haha
        setField(FoodPortionTable.QUANTITY, quantity)
        setField(FoodPortionTable.QUANTITY_UNIT, "g")
        setField(FoodPortionTable.NOTES, "This is an example food portion")
        build(ObjectSource.TEST)
    }

    fp.initFoodAndNd(food)
    return fp
}

fun generateMeal(id: Long, day: DateStamp, foodPortions: List<FoodPortion>): Meal {
    val meal = MacrosBuilder(MealTable).run {
        setField(MealTable.ID, id)
        setField(MealTable.DAY, day)
        setField(MealTable.NAME, "Example meal $id")
        setField(MealTable.NOTES, "Notable notes")
        build(ObjectSource.TEST)
    }

    foodPortions.forEach { meal.addFoodPortion(it) }

    return meal
}