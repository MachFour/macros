package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.makeSqlUpdatePlaceholders
import com.machfour.macros.util.stringJoin

internal class UpdateStatementImpl<M> private constructor(
    table: Table<M>,
    override val columns: List<Column<M, *>>,
    override val whereExpression: SqlWhereExpr<M, *>
): SqlStatementImpl<M>(table, SqlQueryMode.UPDATE), UpdateStatement<M> {
    override fun toSql(): String {
        //return "UPDATE ${table.name} SET $placeholders ${whereExpression.toSql()}"
        return ArrayList<String>().let {
            it.add(mode.sql)
            it.add(table.name)
            it.add("SET")
            it.add(makeSqlUpdatePlaceholders(columns))
            it.add(whereExpression.toSql())
            stringJoin(it, sep = " ")
        }
    }

    internal class Builder<M>(table: Table<M>, val columns: List<Column<M, *>>)
        : SqlStatementImpl.Builder<M>(table), UpdateStatement.Builder<M> {

        fun buildQuery() = UpdateStatementImpl(table, columns, whereExpression)
    }
}
