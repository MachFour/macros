package com.machfour.macros.entities

import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.schema.NutrientGoalValueTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

class NutrientGoalValue internal constructor(
    data: RowData<NutrientGoalValue>,
    objectSource: ObjectSource
) : NutrientValue<NutrientGoalValue>(
    data,
    objectSource,
    NutrientGoalValueTable.NUTRIENT_ID,
    NutrientGoalValueTable.UNIT_ID,
    NutrientGoalValueTable.VALUE,
    NutrientGoalValueTable.CONSTRAINT_SPEC,
) {

    companion object {
        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory: Factory<NutrientGoalValue>
            get() = Factories.nutrientGoalValue

    }

    override val factory: Factory<NutrientGoalValue>
        get() = Companion.factory

    override val table: Table<NutrientGoalValue>
        get() = NutrientGoalValueTable


}