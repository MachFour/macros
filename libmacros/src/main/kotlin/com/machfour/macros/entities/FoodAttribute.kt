package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.schema.FoodAttributeTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

class FoodAttribute private constructor(data: RowData<FoodAttribute>, objectSource: ObjectSource)
    : MacrosEntityImpl<FoodAttribute>(data, objectSource) {

    companion object {
        val factory: Factory<FoodAttribute> = Factory { dataMap, objectSource -> FoodAttribute(dataMap, objectSource) }
    }

    override val factory: Factory<FoodAttribute>
        get() = Companion.factory
    override val table: Table<FoodAttribute>
        get() = FoodAttributeTable


}
