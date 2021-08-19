package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.orm.schema.NutrientGoalTable
import com.machfour.macros.orm.schema.NutrientGoalValueTable
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.nutrientdata.FoodNutrientData
import com.machfour.macros.nutrientdata.GenericNutrientData
import com.machfour.macros.sql.ColumnData
import com.machfour.macros.orm.Factory
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.sql.Table
import com.machfour.macros.util.DateStamp


class NutrientGoal internal constructor(
    data: ColumnData<NutrientGoal>,
    objectSource: ObjectSource
) : MacrosEntityImpl<NutrientGoal>(data, objectSource) {

    companion object {
        val factory: Factory<NutrientGoal>
            get() = Factories.nutrientGoal

        val table: Table<NutrientGoal>
            get() = NutrientGoalTable.instance

        // returns amount of nutrient in food nutrient data divided by goal amount, if both are present
        // and goal is nonzero.
        // If goal value is missing, returns null. If data value is missing or goal value is zero, returns zero.
        fun FoodNutrientData.getProportionOfGoal(goal: NutrientGoal, n: Nutrient): Double? {
            return when (val goalValue = goal.targets[n]?.value) {
                null -> null
                0.0 -> 0.0
                else -> (this[n]?.value ?: 0.0) / goalValue
            }
        }

        fun makeComputedObject(name: String): NutrientGoal {
            val data = ColumnData(table).apply {
                put(NutrientGoalTable.ID, MacrosEntity.NO_ID)
                put(NutrientGoalTable.NAME, name)
            }
            return NutrientGoal(data, ObjectSource.COMPUTED)
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
        val valueData = ColumnData(NutrientGoalValue.table).apply {
            put(NutrientGoalValueTable.ID, MacrosEntity.NO_ID)
            put(NutrientGoalValueTable.GOAL_ID, id)
            put(NutrientGoalValueTable.NUTRIENT_ID, nutrient.id)
            put(NutrientGoalValueTable.VALUE, value)
            put(NutrientGoalValueTable.UNIT_ID, unit.id)
        }
        targets[nutrient] = NutrientGoalValue.factory.construct(valueData, ObjectSource.COMPUTED)
    }
}