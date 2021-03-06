package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.RegularMealTable
import com.machfour.macros.core.schema.SchemaHelpers

class RegularMeal(data: ColumnData<RegularMeal>, objectSource: ObjectSource) : MacrosEntityImpl<RegularMeal>(data, objectSource) {
    companion object {
        val table: Table<RegularMeal>
            get() = RegularMealTable.instance
        val factory: Factory<RegularMeal> = Factory { dataMap, objectSource -> RegularMeal(dataMap, objectSource) }
    }

    override val factory: Factory<RegularMeal>
        get() = Companion.factory

    override val table: Table<RegularMeal>
        get() = Companion.table

}
