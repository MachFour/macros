package com.machfour.macros.entities

import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.schema.FoodAttributeTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

class FoodAttribute private constructor(data: RowData<FoodAttribute>, objectSource: ObjectSource)
    : MacrosEntityImpl<FoodAttribute>(data, objectSource) {

    companion object {
        val factory: Factory<FoodAttribute, FoodAttribute> = object : Factory<FoodAttribute, FoodAttribute> {
            override fun construct(data: RowData<FoodAttribute>, source: ObjectSource): FoodAttribute {
                return FoodAttribute(data, source)
            }

            override fun deconstruct(obj: FoodAttribute): RowData<FoodAttribute> {
                return obj.data
            }
        }
    }

    override fun getTable(): Table<*, FoodAttribute> {
        return FoodAttributeTable
    }

}
