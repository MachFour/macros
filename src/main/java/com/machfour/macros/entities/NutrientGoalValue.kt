package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.NutrientGoalValueTable
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.util.DateStamp

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

        private fun makeBaseComputedValue(value: Double, nutrient: Nutrient, unit: Unit): ColumnData<NutrientGoalValue> {
            return ColumnData(table).apply {
                put(NutrientGoalValueTable.ID, MacrosEntity.NO_ID)
                put(NutrientGoalValueTable.VALUE, value)
                put(NutrientGoalValueTable.NUTRIENT_ID, nutrient.id)
                put(NutrientGoalValueTable.UNIT_ID, unit.id)
            }
        }

        // for day goals
        fun makeComputedValue(date: DateStamp, value: Double, nutrient: Nutrient, unit: Unit): NutrientGoalValue {
            val data = makeBaseComputedValue(value, nutrient, unit)
            data.put(NutrientGoalValueTable.DAY, date)
            return factory.construct(data, ObjectSource.COMPUTED)
        }
        // for meal goals
        fun makeComputedValue(mealId: Long, value: Double, nutrient: Nutrient, unit: Unit): NutrientGoalValue {
            val data = makeBaseComputedValue(value, nutrient, unit)
            data.put(NutrientGoalValueTable.MEAL_ID, mealId)
            return factory.construct(data, ObjectSource.COMPUTED)
        }
    }

    override val factory: Factory<NutrientGoalValue>
        get() = Companion.factory

    override val table: Table<NutrientGoalValue>
        get() = Companion.table


}
