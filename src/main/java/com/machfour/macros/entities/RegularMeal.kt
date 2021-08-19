package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.orm.schema.RegularMealTable
import com.machfour.macros.orm.ColumnData
import com.machfour.macros.orm.Factory
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.sql.Table

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
