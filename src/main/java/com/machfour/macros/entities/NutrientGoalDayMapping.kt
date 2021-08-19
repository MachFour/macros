package com.machfour.macros.entities

import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.orm.Factory
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.NutrientGoalDayMappingTable
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