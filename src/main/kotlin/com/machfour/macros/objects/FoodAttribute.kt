package com.machfour.macros.objects

import com.machfour.macros.core.*

class FoodAttribute private constructor(data: ColumnData<FoodAttribute>, objectSource: ObjectSource)
    : MacrosEntityImpl<FoodAttribute>(data, objectSource) {

    override val table: Table<FoodAttribute>
        get() = Schema.FoodAttributeTable.instance
    override val factory: Factory<FoodAttribute>
        get() = factory()

    companion object {

        fun factory(): Factory<FoodAttribute> {
            return object : Factory<FoodAttribute> {
                override fun construct(dataMap: ColumnData<FoodAttribute>, objectSource: ObjectSource): FoodAttribute {
                    return FoodAttribute(dataMap, objectSource)
                }
            }
        }
    }

}
