package com.machfour.macros.core

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

// Represents objects built using RowData/Table structure
interface MacrosSqlEntity<M: MacrosSqlEntity<M>>: MacrosEntity {
    val table: Table<M>

    val data: RowData<M>

    // returns full copy of row data
    fun toRowData(): RowData<M>

    // Used to get data by column objects
    fun <J : Any> getData(col: Column<M, J>): J? {
        return data[col]
    }

    fun hasData(col: Column<M, *>): Boolean {
        return data.hasValue(col)
    }
}
