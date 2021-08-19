package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.MacrosType

/*
 * Represents a generalised column expression, which may be either a column itself,
 * or some function of a single or aggregated set of values of that column
 * Type parameters:
 *    M still represents the table that the column belongs to
 *    J represents the Kotlin type of the expression, which depending on the expression,
 *      may or may not match the underlying column type
 */
interface ColumnExpr<M, J> {
    val sql: String
    val table: Table<M>
    val type: MacrosType<J>
}