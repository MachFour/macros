package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.schema.FoodAttributeTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

class FoodAttribute private constructor(data: RowData<FoodAttribute>, objectSource: ObjectSource)
    : MacrosEntityImpl<FoodAttribute>(data, objectSource) {

    companion object {
        val factory: Factory<FoodAttribute> = object : Factory<FoodAttribute> {
            override fun construct(data: RowData<FoodAttribute>, source: ObjectSource): FoodAttribute {
                return FoodAttribute(data, source)
            }

            override fun deconstruct(obj: FoodAttribute): RowData<FoodAttribute> {
                TODO("Not yet implemented")
            }
        }
    }

    override val factory: Factory<FoodAttribute>
        get() = Companion.factory
    override val table: Table<FoodAttribute>
        get() = FoodAttributeTable


}
