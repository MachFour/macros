package com.machfour.macros.entities

import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.MacrosSqlEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.nutrients.INutrientValue
import com.machfour.macros.nutrients.nutrientWithId
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.unitWithId

abstract class NutrientValue<M: MacrosSqlEntity<M>> protected constructor(
    data: RowData<M>,
    objectSource: ObjectSource,

    private val nutrientIdCol: Column.Fk<M, Long, Nutrient>,
    /* private val */
    unitIdCol: Column.Fk<M, Long, Unit>,
    /* private val */
    valueCol: Column<M, Double>,
    private val constraintSpecCol: Column<M, Int>,
) : MacrosEntityImpl<M>(data, objectSource), INutrientValue {

    val nutrientId: Long
        get() = data[nutrientIdCol]!!

    val value: Double = this.data[valueCol]!!

    final override val amount: Double = value
    final override val unit: Unit = unitWithId(this.data[unitIdCol]!!)
    final override val nutrient: Nutrient = nutrientWithId(nutrientId)

    final override val constraintSpec: Int
        get() = this.data[constraintSpecCol]!!

}
