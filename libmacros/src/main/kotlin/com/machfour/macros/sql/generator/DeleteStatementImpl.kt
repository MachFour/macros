package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Table

internal class DeleteStatementImpl<M> private constructor(
    table: Table<*, M>,
    override val whereExpression: SqlWhereExpr<M, *>
): SqlStatementImpl<M>(table, SqlQueryMode.DELETE), DeleteStatement<M> {
    override fun toSql(): String {
        // DELETE FROM ${table.name} WHERE ...
        return ArrayList<String>().let {
            it.add(mode.sql)
            it.add("FROM")
            it.add(table.sqlName)
            it.add(whereExpression.toSql())
            it.joinToString(separator = " ")
        }
    }

    internal class Builder<M>(table: Table<*, M>)
        : SqlStatementImpl.Builder<M>(table), DeleteStatement.Builder<M> {

        fun buildQuery() = DeleteStatementImpl(table, whereExpression)
    }
}
