package com.machfour.macros.sample

import com.machfour.datestamp.DateStamp
import com.machfour.datestamp.makeDateStamp
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.MealTable
import com.machfour.macros.sql.rowdata.RowData

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
    val fp = RowData(FoodPortionTable).run {
        put(FoodPortionTable.ID, id)
        put(FoodPortionTable.MEAL_ID, mealId)
        put(FoodPortionTable.FOOD_ID, food.id) // who knows what food this is haha
        put(FoodPortionTable.QUANTITY, quantity)
        put(FoodPortionTable.QUANTITY_UNIT, "g")
        put(FoodPortionTable.NOTES, "This is an example food portion")
        FoodPortion.factory.construct(this, ObjectSource.TEST)
    }

    fp.initFoodAndNd(food)
    return fp
}

fun generateMeal(id: Long, day: DateStamp, foodPortions: List<FoodPortion>): Meal {
    val meal = RowData(MealTable).run {
        put(MealTable.ID, id)
        put(MealTable.DAY, day)
        put(MealTable.NAME, "Example meal $id")
        put(MealTable.NOTES, "Notable notes")
        Meal.factory.construct(this, ObjectSource.TEST)
    }

    foodPortions.forEach { meal.addFoodPortion(it) }

    return meal
}