package com.machfour.macros.validation

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table


typealias ErrorMap<M> = Map<Column<M, *>, List<ValidationError>>
typealias MutableErrorMap<M> = MutableMap<Column<M, *>, List<ValidationError>>

interface MacrosValidator<M> {
    val table: Table<M>

    // Validates all columns in the table
    // in addition to validating the data, checks for any other missing table columns
    // which are not nullable and do not have default values
    fun validateAsTableRow(data: RowData<M>): ErrorMap<M>

    // Validates all the columns in the data object
    // If any violations are found, the affected column as well as an enum value describing the violation are recorded
    // in a map, which is returned at the end, after all columns have been processed.
    // There are no validation errors if and only if the returned map is empty
    fun validateData(data: RowData<M>): ErrorMap<M>

    // Returns validation errors just for a single column.
    // There are no errors if and only if the returned list is empty.
    fun <J> validateSingle(data: RowData<M>, col: Column<M, J>): List<ValidationError>


}