package com.machfour.macros.sql.generator

interface DeleteStatement<M>: SqlStatement<M> {

    interface Builder<M>: SqlStatement.Builder<M> {
    }
}

