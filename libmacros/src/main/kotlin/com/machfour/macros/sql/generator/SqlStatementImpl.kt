package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

abstract class SqlStatementImpl<M>(
    override val table: Table<*, M>,
    override val mode: SqlQueryMode,
): SqlStatement<M> {

    abstract override val whereExpression: SqlWhereExpr<M, *>

    override val shouldIterateBindArguments: Boolean
        get() = whereExpression.isIterated

    override val hasBindArguments: Boolean
        get() = whereExpression.hasBindObjects

    override fun getBindArguments(): Collection<Any> {
        return whereExpression.getRawBindObjects()
    }

    abstract override fun toSql(): String

    abstract class Builder<M>(protected val table: Table<*, M>): SqlStatement.Builder<M> {
        protected var whereExpression: SqlWhereExpr<M, *> = SqlWhereExpr.whereAny()

        override fun <J: Any> where(whereColumnExpr: ColumnExpr<M, J>, whereValue: J) {
            whereExpression = SqlWhereExpr.where(whereColumnExpr, whereValue)
        }

        override fun <J: Any> where(whereColumnExpr: ColumnExpr<M, J>, whereValues: Collection<J>, iterateThreshold: Int) {
            whereExpression = SqlWhereExpr.where(whereColumnExpr, whereValues, whereValues.size > iterateThreshold)
        }

        override fun whereNull(whereColumnExpr: ColumnExpr<M, *>, negate: Boolean) {
            whereExpression = SqlWhereExpr.whereNull(whereColumnExpr, !negate)
        }

        override fun where(whereString: String) {
            whereExpression = SqlWhereExpr.where(whereString)
        }

        override fun andWhere(expr: String) {
            whereExpression.alsoWhere(Conjunction.AND, expr)
        }
        override fun orWhere(expr: String) {
            whereExpression.alsoWhere(Conjunction.OR, expr)
        }

        override fun whereLike(
            likeColumns: Collection<Column<M, String>>,
            whereLikeValues: Collection<String>,
            conjunction: Conjunction
        ) {
            whereExpression = SqlWhereExpr.whereLike(likeColumns, whereLikeValues, conjunction)
        }

    }
}
