package com.machfour.macros.sql

import com.machfour.macros.orm.Column
import com.machfour.macros.orm.Table

abstract class SqlStatementImpl<M>(
    override val table: Table<M>,
    override val mode: SqlQueryMode,
    final override val whereExpression: SqlWhereExpr<M, *>
): SqlStatement<M> {

    override val shouldIterateBindArguments: Boolean
        get() = whereExpression.isIterated

    override val hasBindArguments: Boolean
        get() = whereExpression.numArgs > 0

    override fun getBindArguments(): Collection<*> {
        return whereExpression.getBindObjects()
    }


    abstract override fun toSql(): String

    abstract class Builder<M>(protected val table: Table<M>): SqlStatement.Builder<M> {
        protected var whereExpression: SqlWhereExpr<M, *> = SqlWhereExpr.whereAny()

        override fun <J> where(whereColumnExpr: ColumnExpr<M, J>, whereValue: J) {
            whereExpression = SqlWhereExpr.where(whereColumnExpr, whereValue)
        }

        override fun <J> where(whereColumnExpr: ColumnExpr<M, J>, whereValues: Collection<J>, iterate: Boolean) {
            whereExpression = SqlWhereExpr.where(whereColumnExpr, whereValues, iterate)
        }

        override fun where(whereColumnExpr: ColumnExpr<M, *>, isNotNull: Boolean) {
            whereExpression = SqlWhereExpr.where(whereColumnExpr, isNotNull)
        }

        override fun where(whereString: String) {
            whereExpression = SqlWhereExpr.where(whereString)
        }

        override fun whereLike(
            likeColumns: Collection<Column<M, String>>,
            whereLikeValues: Collection<String>,
            conjunction: Conjuction
        ) {
            whereExpression = SqlWhereExpr.whereLike(likeColumns, whereLikeValues, conjunction)
        }

    }
}
