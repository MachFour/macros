package com.machfour.macros.core

fun interface Factory<M> {
    fun construct(data: ColumnData<M>, objectSource: ObjectSource): M
}