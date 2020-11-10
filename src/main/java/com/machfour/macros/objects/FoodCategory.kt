package com.machfour.macros.objects

import com.machfour.macros.core.*

class FoodCategory(data: ColumnData<FoodCategory>, objectSource: ObjectSource) : MacrosEntityImpl<FoodCategory>(data, objectSource) {
    companion object {
        val factory: Factory<FoodCategory> = Factory { dataMap, objectSource -> FoodCategory(dataMap, objectSource) }
        val table: Table<FoodCategory>
            get() = Schema.FoodCategoryTable.instance
    }

    override val factory: Factory<FoodCategory>
        get() = Companion.factory

    override val table: Table<FoodCategory>
        get() = Companion.table

    val name: String
        get() = getData(Schema.FoodCategoryTable.NAME)!!

    override fun toString(): String {
        return name
    }
}
