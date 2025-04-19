package com.machfour.macros.entities

import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.schema.FoodCategoryTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

class FoodCategory(data: RowData<FoodCategory>, objectSource: ObjectSource) : MacrosEntityImpl<FoodCategory>(data, objectSource) {
    companion object {
        val factory: Factory<FoodCategory, FoodCategory>
            get() = Factories.foodCategory
    }

    override fun getTable(): Table<*, FoodCategory> {
        return FoodCategoryTable
    }

    val name: String
        get() = data[FoodCategoryTable.NAME]!!

    override fun toString(): String {
        return name
    }
}
