package com.machfour.macros.objects

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.Factory
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Table
import com.machfour.macros.core.schema.MealNutrientGoalValueTable
import com.machfour.macros.objects.helpers.Factories

class MealNutrientGoalValue internal constructor(
    data: ColumnData<MealNutrientGoalValue>,
    objectSource: ObjectSource
) : NutrientValue<MealNutrientGoalValue>(
    data,
    objectSource,
    MealNutrientGoalValueTable.NUTRIENT_ID,
    MealNutrientGoalValueTable.UNIT_ID,
    MealNutrientGoalValueTable.VALUE,
    MealNutrientGoalValueTable.CONSTRAINT_SPEC,
) {

    companion object {
        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory : Factory<MealNutrientGoalValue>
            get() = Factories.mealNutrientGoalValue

        val table: Table<MealNutrientGoalValue>
            get() = MealNutrientGoalValueTable.instance

    }

    override val factory: Factory<MealNutrientGoalValue>
        get() = Companion.factory

    override val table: Table<MealNutrientGoalValue>
        get() = Companion.table


}
