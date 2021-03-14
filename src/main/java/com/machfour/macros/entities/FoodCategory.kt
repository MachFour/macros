package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.FoodCategoryTable

class FoodCategory(data: ColumnData<FoodCategory>, objectSource: ObjectSource) : MacrosEntityImpl<FoodCategory>(data, objectSource) {
    companion object {
        val factory: Factory<FoodCategory> = Factory { dataMap, objectSource -> FoodCategory(dataMap, objectSource) }
        val table: Table<FoodCategory>
            get() = FoodCategoryTable.instance
    }

    override val factory: Factory<FoodCategory>
        get() = Companion.factory

    override val table: Table<FoodCategory>
        get() = Companion.table

    val name: String
        get() = getData(FoodCategoryTable.NAME)!!

    override fun toString(): String {
        return name
    }
}
