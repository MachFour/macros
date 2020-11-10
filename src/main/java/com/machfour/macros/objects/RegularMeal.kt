package com.machfour.macros.objects

import com.machfour.macros.core.*

class RegularMeal(data: ColumnData<RegularMeal>, objectSource: ObjectSource) : MacrosEntityImpl<RegularMeal>(data, objectSource) {
    companion object {
        val table: Table<RegularMeal>
            get() = Schema.RegularMealTable.instance
        val factory: Factory<RegularMeal> = Factory { dataMap, objectSource -> RegularMeal(dataMap, objectSource) }
    }

    override val factory: Factory<RegularMeal>
        get() = Companion.factory

    override val table: Table<RegularMeal>
        get() = Companion.table

}
