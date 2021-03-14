package com.machfour.macros.nutrientdata

import com.machfour.macros.entities.DayNutrientGoalValue
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.MealNutrientGoalValue

class MealNutrientGoal(
    val meal: Meal
): GenericNutrientData<MealNutrientGoalValue>(dataCompleteIfNotNull = true) {
    
}