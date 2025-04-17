package com.machfour.macros.validation

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.rowdata.RowData

typealias ErrorMap<M> = Map<Column<M, *>, List<ValidationError>>

private fun <M, J: Any> isInvalidValue(data: RowData<M>, col: Column<M, J>): Boolean {
    return data[col] == null && !col.isNullable
}

fun <M, J: Any> validateNonNull(data: RowData<M>, col: Column<M, J>): List<ValidationError> {
    return if (isInvalidValue(data, col)) listOf(ValidationError.NON_NULL) else emptyList()
}

fun <M> validateNonNull(data: RowData<M>): ErrorMap<M> {
    return buildMap {
        for (col in data.columns) {
            if (isInvalidValue(data, col)) {
                put(col, listOf(ValidationError.NON_NULL))
            }
        }
    }
}