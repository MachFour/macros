package com.machfour.macros.nutrientdata

import com.machfour.macros.entities.DayNutrientGoalValue
import com.machfour.macros.util.DateStamp

class DayNutrientGoal(
    val day: DateStamp
): GenericNutrientData<DayNutrientGoalValue>(dataCompleteIfNotNull = true) {
    
}