package com.machfour.macros.objects

import com.machfour.macros.core.*

class RegularMeal(data: ColumnData<RegularMeal>, objectSource: ObjectSource) : MacrosEntityImpl<RegularMeal>(data, objectSource) {

    override val table: Table<RegularMeal>
        get() = Schema.RegularMealTable.instance

    override val factory: Factory<RegularMeal>
        get() = factory()

    companion object {

        fun factory(): Factory<RegularMeal> {
            return object : Factory<RegularMeal> {
                override fun construct(dataMap: ColumnData<RegularMeal>, objectSource: ObjectSource): RegularMeal {
                    return RegularMeal(dataMap, objectSource)
                }
            }
        }
    }
}
