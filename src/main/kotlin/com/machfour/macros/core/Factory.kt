package com.machfour.macros.core

import com.machfour.macros.sql.RowData

fun interface Factory<M> {
    fun construct(data: RowData<M>, source: ObjectSource): M
}