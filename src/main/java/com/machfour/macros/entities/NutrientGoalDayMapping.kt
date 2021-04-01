package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.AttrMappingTable
import com.machfour.macros.core.schema.NutrientGoalDayMappingTable
import com.machfour.macros.core.schema.NutrientGoalTable
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.nutrientdata.GenericNutrientData
import com.machfour.macros.util.DateStamp


class NutrientGoalDayMapping internal constructor(
    data: ColumnData<NutrientGoalDayMapping>,
    objectSource: ObjectSource
) : MacrosEntityImpl<NutrientGoalDayMapping>(data, objectSource) {

    companion object {
    val factory: Factory<NutrientGoalDayMapping>
        get() = Factories.nutrientGoalDayMapping

    val table: Table<NutrientGoalDayMapping>
        get() = NutrientGoalDayMappingTable.instance
    }

    override val factory: Factory<NutrientGoalDayMapping>
        get() = Companion.factory

    override val table: Table<NutrientGoalDayMapping>
        get() = Companion.table

    val day: DateStamp
        get() = getData(NutrientGoalDayMappingTable.DAY)!!

}