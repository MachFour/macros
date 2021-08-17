package com.machfour.macros.sql

import com.machfour.macros.orm.Table

interface DeleteStatement<M>: SqlStatement<M>

class DeleteStatementImpl<M>(
    table: Table<M>,
    whereExpr: SqlWhereExpr<M, *>
): SqlStatementImpl<M>(table, SqlQueryMode.DELETE, whereExpr), DeleteStatement<M> {
    override fun toSql(): String {
        TODO("Not yet implemented")
    }

}
