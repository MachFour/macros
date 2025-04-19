package com.machfour.macros.entities

import com.machfour.datestamp.DateStamp
import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.nutrients.GenericNutrientData
import com.machfour.macros.schema.NutrientGoalTable
import com.machfour.macros.schema.NutrientGoalValueTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData


class NutrientGoal internal constructor(
    data: RowData<NutrientGoal>,
    objectSource: ObjectSource
): MacrosEntityImpl<NutrientGoal>(data, objectSource) {

    companion object {
        val factory: Factory<*, NutrientGoal>
            get() = Factories.nutrientGoal

        fun makeComputedObject(name: String): NutrientGoal {
            val data = RowData(NutrientGoalTable).apply {
                put(NutrientGoalTable.ID, MacrosEntity.NO_ID)
                put(NutrientGoalTable.NAME, name)
            }
            return factory.construct(data, ObjectSource.COMPUTED)
        }

    }

    override fun getTable(): Table<*, NutrientGoal> {
        return NutrientGoalTable
    }

    val name: String
        get() = data[NutrientGoalTable.NAME]!!

    lateinit var day: DateStamp
        private set

    var meal: Meal? = null
        private set

    override fun toString(): String {
        return name
    }

    val targets = object: GenericNutrientData<NutrientGoalValue>() { }

    // make value without ID or nutrition goal object
    fun addComputedValue(nutrient: Nutrient, value: Double, unit: Unit) {
        val valueData = RowData(NutrientGoalValueTable).apply {
            put(NutrientGoalValueTable.ID, MacrosEntity.NO_ID)
            put(NutrientGoalValueTable.GOAL_ID, id)
            put(NutrientGoalValueTable.NUTRIENT_ID, nutrient.id)
            put(NutrientGoalValueTable.VALUE, value)
            put(NutrientGoalValueTable.UNIT_ID, unit.id)
        }
        targets[nutrient] = NutrientGoalValue.factory.construct(valueData, ObjectSource.COMPUTED)
    }
}