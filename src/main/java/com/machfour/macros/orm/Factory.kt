package com.machfour.macros.orm

import com.machfour.macros.sql.ColumnData

fun interface Factory<M> {
    fun construct(data: ColumnData<M>, objectSource: ObjectSource): M
}