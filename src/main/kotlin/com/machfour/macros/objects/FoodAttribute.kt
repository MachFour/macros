package com.machfour.macros.objects

import com.machfour.macros.core.*

class FoodAttribute private constructor(data: ColumnData<FoodAttribute>, objectSource: ObjectSource)
    : MacrosEntityImpl<FoodAttribute>(data, objectSource) {

    companion object {
        val table: Table<FoodAttribute>
            get() = Schema.FoodAttributeTable.instance
        val factory: Factory<FoodAttribute> = Factory { dataMap, objectSource -> FoodAttribute(dataMap, objectSource) }
    }

    override val factory: Factory<FoodAttribute>
        get() = Companion.factory
    override val table: Table<FoodAttribute>
        get() = Companion.table



}
