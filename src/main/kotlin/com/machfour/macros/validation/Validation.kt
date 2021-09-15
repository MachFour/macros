package com.machfour.macros.validation

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData

interface Validation<M> {
    fun validate(data: RowData<M>): Map<Column<M, *>, List<ValidationError>>
    val relevantColumns: Set<Column<M, *>>
}