package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.FoodAttributeTable
import com.machfour.macros.core.schema.SchemaHelpers

class FoodAttribute private constructor(data: ColumnData<FoodAttribute>, objectSource: ObjectSource)
    : MacrosEntityImpl<FoodAttribute>(data, objectSource) {

    companion object {
        val table: Table<FoodAttribute>
            get() = FoodAttributeTable.instance
        val factory: Factory<FoodAttribute> = Factory { dataMap, objectSource -> FoodAttribute(dataMap, objectSource) }
    }

    override val factory: Factory<FoodAttribute>
        get() = Companion.factory
    override val table: Table<FoodAttribute>
        get() = Companion.table



}
