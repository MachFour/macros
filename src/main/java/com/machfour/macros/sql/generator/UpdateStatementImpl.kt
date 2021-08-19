package com.machfour.macros.sql.generator

import com.machfour.macros.persistence.DatabaseUtils
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

internal open class UpdateStatementImpl<M>(
    table: Table<M>,
    override val columns: List<Column<M, *>>,
    override val whereExpression: SqlWhereExpr<M, *>
): SqlStatementImpl<M>(table, SqlQueryMode.UPDATE), UpdateStatement<M> {
    override fun toSql(): String {
        val query = ArrayList<String>()
        query.add(mode.sql)
        query.add(table.name)
        query.add("SET")
        val placeholders = DatabaseUtils.makeUpdatePlaceholders(columns)
        return "UPDATE ${table.name} SET $placeholders ${whereExpression.toSql()}"
    }

    internal open class Builder<M>(
        table: Table<M>,
        val columns: List<Column<M, *>>
    ): SqlStatementImpl.Builder<M>(table), UpdateStatement.Builder<M> {
        fun buildQuery(): UpdateStatementImpl<M> {
            return UpdateStatementImpl(
                table = table,
                columns = columns,
                whereExpression = whereExpression,
            )
        }
    }
}
