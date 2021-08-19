package com.machfour.macros.entities

import com.machfour.macros.orm.schema.NutrientGoalValueTable
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.ColumnData
import com.machfour.macros.orm.Factory
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.sql.Table

class NutrientGoalValue internal constructor(
    data: ColumnData<NutrientGoalValue>,
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
        val factory : Factory<NutrientGoalValue>
            get() = Factories.nutrientGoalValue

        val table: Table<NutrientGoalValue>
            get() = NutrientGoalValueTable.instance

    }

    override val factory: Factory<NutrientGoalValue>
        get() = Companion.factory

    override val table: Table<NutrientGoalValue>
        get() = Companion.table


}
