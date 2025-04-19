package com.machfour.macros.sql.entities

import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.rowdata.RowData

interface Factory<M> {
    fun construct(data: RowData<M>, source: ObjectSource): M
    fun deconstruct(obj: M): RowData<M>
}