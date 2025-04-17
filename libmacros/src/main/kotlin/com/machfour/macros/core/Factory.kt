package com.machfour.macros.core

import com.machfour.macros.sql.rowdata.RowData

interface Factory<M> {
    fun construct(data: RowData<M>, source: ObjectSource): M
    fun deconstruct(obj: M): RowData<M>
}