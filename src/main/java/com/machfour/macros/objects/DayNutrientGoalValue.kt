package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.DayNutrientGoalValueTable
import com.machfour.macros.objects.helpers.Factories

class DayNutrientGoalValue internal constructor(
    data: ColumnData<DayNutrientGoalValue>,
    objectSource: ObjectSource
) : NutrientValue<DayNutrientGoalValue>(
    data,
    objectSource,
    DayNutrientGoalValueTable.NUTRIENT_ID,
    DayNutrientGoalValueTable.UNIT_ID,
    DayNutrientGoalValueTable.VALUE,
    DayNutrientGoalValueTable.CONSTRAINT_SPEC
) {

    companion object {
        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory : Factory<DayNutrientGoalValue>
            get() = Factories.dayNutrientGoalValue

        val table: Table<DayNutrientGoalValue>
            get() = DayNutrientGoalValueTable.instance

    }

    override val factory: Factory<DayNutrientGoalValue>
        get() = Companion.factory

    override val table: Table<DayNutrientGoalValue>
        get() = Companion.table

}
