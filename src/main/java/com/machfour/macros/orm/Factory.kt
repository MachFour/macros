package com.machfour.macros.orm

fun interface Factory<M> {
    fun construct(data: ColumnData<M>, objectSource: ObjectSource): M
}