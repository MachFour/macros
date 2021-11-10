package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.GenericNutrientData
import com.machfour.macros.schema.NutrientGoalTable
import com.machfour.macros.schema.NutrientGoalValueTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.units.StandardNutrientUnits
import com.machfour.macros.util.DateStamp


class NutrientGoal internal constructor(
    data: RowData<NutrientGoal>,
    objectSource: ObjectSource
): MacrosEntityImpl<NutrientGoal>(data, objectSource) {

    companion object {
        val factory: Factory<NutrientGoal>
            get() = Factories.nutrientGoal

        val table: Table<NutrientGoal>
            get() = NutrientGoalTable

        // returns amount of nutrient in food nutrient data divided by goal amount, if both are present
        // and goal is nonzero.
        // If goal value is missing, returns null. If data value is missing or goal value is zero, returns zero.
        fun FoodNutrientData.getProportionOfGoal(goal: NutrientGoal, n: Nutrient): Double? {
            return when (val goalValue = goal.targets[n]?.value) {
                null -> null
                0.0 -> 0.0
                else -> {
                    // goal value is in standard units
                    (this[n]?.convertValueTo(StandardNutrientUnits[n]) ?: 0.0) / goalValue
                }
            }
        }

        fun makeComputedObject(name: String): NutrientGoal {
            val data = RowData(table).apply {
                put(NutrientGoalTable.ID, MacrosEntity.NO_ID)
                put(NutrientGoalTable.NAME, name)
            }
            return factory.construct(data, ObjectSource.COMPUTED)
        }

    }

    override val factory: Factory<NutrientGoal>
        get() = Companion.factory

    override val table: Table<NutrientGoal>
        get() = Companion.table

    val name: String
        get() = getData(NutrientGoalTable.NAME)!!

    lateinit var day: DateStamp
        private set

    var meal: Meal? = null
        private set

    override fun toString(): String {
        return name
    }

    val targets = GenericNutrientData<NutrientGoalValue>(dataCompleteIfNotNull = true)

    // make value without ID or nutrition goal object
    fun addComputedValue(nutrient: Nutrient, value: Double, unit: Unit) {
        val valueData = RowData(NutrientGoalValue.table).apply {
            put(NutrientGoalValueTable.ID, MacrosEntity.NO_ID)
            put(NutrientGoalValueTable.GOAL_ID, id)
            put(NutrientGoalValueTable.NUTRIENT_ID, nutrient.id)
            put(NutrientGoalValueTable.VALUE, value)
            put(NutrientGoalValueTable.UNIT_ID, unit.id)
        }
        targets[nutrient] = NutrientGoalValue.factory.construct(valueData, ObjectSource.COMPUTED)
    }
}