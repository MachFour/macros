package com.machfour.macros.core;

public interface Factory<M> {
    M construct(ColumnData<M> dataMap, ObjectSource objectSource);
}
