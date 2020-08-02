package com.machfour.macros.core

fun interface Factory<M> {
    fun construct(dataMap: ColumnData<M>, objectSource: ObjectSource): M
}