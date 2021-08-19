package com.machfour.macros.orm

import com.machfour.macros.sql.RowData

fun interface Factory<M> {
    fun construct(data: RowData<M>, objectSource: ObjectSource): M
}