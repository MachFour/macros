package com.machfour.macros.data;

import com.machfour.macros.core.MacrosPersistable;

import java.util.List;
import java.util.Map;

public interface Table<M extends MacrosPersistable> {
    String name();

    List<Column<M, ?>> columns();

    Map<String, Column<M, ?>> columnsByName();

    Column<M, ?> columnForName(String name);

    Column<M, Long> getIdColumn();

    Column<M, Long> getCreateTimeColumn();

    Column<M, Long> getModifyTimeColumn();

    M construct(ColumnData<M> dataMap, boolean isFromDb);
}
