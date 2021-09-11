package com.machfour.macros.validation

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData

fun interface Validation<M> {
    fun validate(data: RowData<M>): Map<Column<M, *>, List<ValidationError>>
}