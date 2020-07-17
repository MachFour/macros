package com.machfour.macros.objects

import com.machfour.macros.core.*

class FoodCategory(data: ColumnData<FoodCategory>, objectSource: ObjectSource) : MacrosEntityImpl<FoodCategory>(data, objectSource) {

    override val table: Table<FoodCategory>
        get() = table()

    val name: String
        get() = getData(Schema.FoodCategoryTable.NAME)!!

    override val factory: Factory<FoodCategory>
        get() = factory()

    override fun toString(): String {
        return name
    }

    companion object {

        fun factory(): Factory<FoodCategory> {
            return object : Factory<FoodCategory> {
                override fun construct(dataMap: ColumnData<FoodCategory>, objectSource: ObjectSource): FoodCategory {
                    return FoodCategory(dataMap, objectSource)
                }
            }
        }

        fun table(): Table<FoodCategory> {
            return Schema.FoodCategoryTable.instance
        }
    }
}
