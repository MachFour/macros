package com.machfour.macros.sql

import com.machfour.macros.core.Table

open class SqlQuery<M>(
    val table: Table<M>,
    val mode: SqlQueryMode,
) {

}