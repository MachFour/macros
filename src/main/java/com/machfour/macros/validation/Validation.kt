package com.machfour.macros.validation

import com.machfour.macros.core.Column
import com.machfour.macros.core.ColumnData

fun interface Validation<M> {
    fun validate(data: ColumnData<M>): Map<Column<M, *>, List<ValidationError>>
}