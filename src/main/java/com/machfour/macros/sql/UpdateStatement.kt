package com.machfour.macros.sql

import com.machfour.macros.orm.Column
import com.machfour.macros.orm.Table

interface UpdateStatement<M>: SqlStatement<M>

internal open class UpdateStatementImpl<M>(
    table: Table<M>,
    val columns: List<Column<M, *>>,
    whereExpr: SqlWhereExpr<M, *>
): SqlStatementImpl<M>(table, SqlQueryMode.UPDATE, whereExpr), UpdateStatement<M> {
    override fun toSql(): String {
        TODO("Not yet implemented")
    }
}
