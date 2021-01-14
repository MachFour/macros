package com.machfour.macros.objects

import com.machfour.macros.core.Column
import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Units

abstract class NutrientValue<M: NutrientValue<M>> protected constructor(
    data: ColumnData<M>,
    objectSource: ObjectSource,

    private val nutrientIdCol: Column.Fk<M, Long, Nutrient>,
    private val unitIdCol: Column.Fk<M, Long, Unit>,
    private val valueCol: Column<M, Double>,
    private val constraintSpecCol: Column<M, Int>,
) : MacrosEntityImpl<M>(data, objectSource) {


    val nutrientId: Long
        get() = getData(nutrientIdCol)!!

    val value: Double = getData(valueCol)!!
    val unit: Unit = Units.fromId(getData(unitIdCol)!!)
    val nutrient: Nutrient = Nutrients.fromId(nutrientId)
}
