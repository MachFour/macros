package com.machfour.macros.core

interface Factory<M> {
    fun construct(dataMap: ColumnData<M>, objectSource: ObjectSource): M
}