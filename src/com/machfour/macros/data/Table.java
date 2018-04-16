package com.machfour.macros.data;

import com.machfour.macros.core.MacrosPersistable;

import java.util.List;
import java.util.Map;

public interface Table<T extends MacrosPersistable> {
    String name();

    List<Column<?>> columns();

    Map<String, Column<?>> columnsByName();

    Column<?> columnForName(String name);

    T construct(ColumnData<T> dataMap, boolean isFromDb);
}
