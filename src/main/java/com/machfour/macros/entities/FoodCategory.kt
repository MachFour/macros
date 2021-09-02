package com.machfour.macros.entities

import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.orm.Factory
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.FoodCategoryTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

class FoodCategory(data: RowData<FoodCategory>, objectSource: ObjectSource) : MacrosEntityImpl<FoodCategory>(data, objectSource) {
    companion object {
        val factory: Factory<FoodCategory>
            get() = Factories.foodCategory
        val table: Table<FoodCategory>
            get() = FoodCategoryTable
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
