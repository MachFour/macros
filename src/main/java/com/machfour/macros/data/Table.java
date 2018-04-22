package com.machfour.macros.data;

import java.util.List;
import java.util.Map;

public interface Table<M> {
    String name();

    List<Column<M, ?, ?>> columns();

    Map<String, Column<M, ?, ?>> columnsByName();

    Column<M, ?, ?> columnForName(String name);

    Column<M, Types.Id, Long> getIdColumn();

    Column<M, Types.Time, Long> getCreateTimeColumn();

    Column<M, Types.Time, Long> getModifyTimeColumn();

    M construct(ColumnData<M> dataMap, boolean isFromDb);
}
