package com.machfour.macros.entities

import com.machfour.datestamp.DateStamp
import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.schema.NutrientGoalDayMappingTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData


class NutrientGoalDayMapping internal constructor(
    data: RowData<NutrientGoalDayMapping>,
    objectSource: ObjectSource
) : MacrosEntityImpl<NutrientGoalDayMapping>(data, objectSource) {

    companion object {
        val factory: Factory<NutrientGoalDayMapping>
            get() = Factories.nutrientGoalDayMapping

    }

    override fun getTable(): Table<NutrientGoalDayMapping> {
        return NutrientGoalDayMappingTable
    }

    val day: DateStamp
        get() = data[NutrientGoalDayMappingTable.DAY]!!

}