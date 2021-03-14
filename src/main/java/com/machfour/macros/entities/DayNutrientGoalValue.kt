package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.DayNutrientGoalValueTable
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.util.DateStamp

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

        fun makeComputedValue(date: DateStamp, value: Double, nutrient: Nutrient, unit: Unit): DayNutrientGoalValue {
            val data = ColumnData(table).apply {
                //put(NutrientValueTable.ID, id ?: MacrosEntity.NO_ID)
                //put(NutrientValueTable.FOOD_ID, food?.id ?: MacrosEntity.NO_ID)
                put(DayNutrientGoalValueTable.DAY, date)
                put(DayNutrientGoalValueTable.ID, MacrosEntity.NO_ID)
                put(DayNutrientGoalValueTable.VALUE, value)
                put(DayNutrientGoalValueTable.NUTRIENT_ID, nutrient.id)
                put(DayNutrientGoalValueTable.UNIT_ID, unit.id)
            }
            return factory.construct(data, ObjectSource.COMPUTED)
        }

    }

    override val factory: Factory<DayNutrientGoalValue>
        get() = Companion.factory

    override val table: Table<DayNutrientGoalValue>
        get() = Companion.table

    val day: DateStamp
        get() = getData(DayNutrientGoalValueTable.DAY)!!
}
