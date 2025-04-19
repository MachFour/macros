package com.machfour.macros.sql.entities

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.rowdata.RowData

interface Factory<I: MacrosEntity, M: I> {
    fun construct(data: RowData<M>, source: ObjectSource): M
    fun deconstruct(obj: I): RowData<M>
}

interface Deconstructor<I: MacrosEntity, M: I> { }