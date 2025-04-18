package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.schema.FoodCategoryTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

class FoodCategory(data: RowData<FoodCategory>, objectSource: ObjectSource) : MacrosEntityImpl<FoodCategory>(data, objectSource) {
    companion object {
        val factory: Factory<FoodCategory>
            get() = Factories.foodCategory
    }

    override val factory: Factory<FoodCategory>
        get() = Companion.factory

    override val table: Table<FoodCategory>
        get() = FoodCategoryTable

    val name: String
        get() = data[FoodCategoryTable.NAME]!!

    override fun toString(): String {
        return name
    }
}
