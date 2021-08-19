package com.machfour.macros.core

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.validation.Validation


interface MacrosValidator<M: MacrosEntity<M>> {

    // describes how to retrieve the data to be validated
    fun setFieldData(getColumn: (Column<M, *>) -> Any)

    // sets the data source, in case database reads are required for validation
    fun setDataSource(ds: SqlDatabase)

    fun validateAll() : Map<Column<M, *>, VErrorList>

    fun <J> validateSingle(col: Column<M, J>) : VErrorList

    fun addCustomValidation(v: Validation<M>)


}