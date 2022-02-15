package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.schema.RegularMealTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

class RegularMeal(data: RowData<RegularMeal>, objectSource: ObjectSource) : MacrosEntityImpl<RegularMeal>(data, objectSource) {
    companion object {
        val factory: Factory<RegularMeal> = Factory { dataMap, objectSource -> RegularMeal(dataMap, objectSource) }
    }

    override val factory: Factory<RegularMeal>
        get() = Companion.factory

    override val table: Table<RegularMeal>
        get() = RegularMealTable

}
