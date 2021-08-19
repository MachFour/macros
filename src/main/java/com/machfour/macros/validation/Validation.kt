package com.machfour.macros.validation

import com.machfour.macros.sql.Column
import com.machfour.macros.orm.ColumnData

fun interface Validation<M> {
    fun validate(data: ColumnData<M>): Map<Column<M, *>, List<ValidationError>>
}