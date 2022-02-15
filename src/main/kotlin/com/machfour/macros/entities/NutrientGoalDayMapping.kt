package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.schema.NutrientGoalDayMappingTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.util.DateStamp


class NutrientGoalDayMapping internal constructor(
    data: RowData<NutrientGoalDayMapping>,
    objectSource: ObjectSource
) : MacrosEntityImpl<NutrientGoalDayMapping>(data, objectSource) {

    companion object {
        val factory: Factory<NutrientGoalDayMapping>
            get() = Factories.nutrientGoalDayMapping

    }

    override val factory: Factory<NutrientGoalDayMapping>
        get() = Companion.factory

    override val table: Table<NutrientGoalDayMapping>
        get() = NutrientGoalDayMappingTable

    val day: DateStamp
        get() = data[NutrientGoalDayMappingTable.DAY]!!

}