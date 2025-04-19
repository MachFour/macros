package com.machfour.macros.entities

import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.schema.RegularMealTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

class RegularMeal(data: RowData<RegularMeal>, objectSource: ObjectSource) : MacrosEntityImpl<RegularMeal>(data, objectSource) {
    companion object {
        val factory: Factory<RegularMeal> = object: Factory<RegularMeal> {
            override fun construct(data: RowData<RegularMeal>, source: ObjectSource): RegularMeal {
                 return RegularMeal(data, source)
            }
            override fun deconstruct(obj: RegularMeal): RowData<RegularMeal> {
                TODO()
            }
        }
    }

    override fun getTable(): Table<RegularMeal> {
        return RegularMealTable
    }
}
