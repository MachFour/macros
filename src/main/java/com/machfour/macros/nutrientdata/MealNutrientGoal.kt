package com.machfour.macros.nutrientdata

import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.NutrientGoalValue
import com.machfour.macros.util.DateStamp

class NutrientGoal(
    val day: DateStamp,
    val meal: Meal? = null,
): GenericNutrientData<NutrientGoalValue>(dataCompleteIfNotNull = true) {
    
}