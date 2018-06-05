package com.machfour.macros.data;

import com.machfour.macros.core.ObjectSource;

import java.util.List;
import java.util.Map;

public interface Table<M> {
    String name();

    List<Column<M, ?>> columns();

    Map<String, Column<M, ?>> columnsByName();

    Column<M, ?> columnForName(String name);

    Column<M, Long> getIdColumn();

    Column<M, Long> getCreateTimeColumn();

    Column<M, Long> getModifyTimeColumn();

    // returns a list of columns that can be used to identify an individual row,
    // if such a list exists for this table. If not, an empty list is returned.
    List<Column<M, ?>> getSecondaryKey();

    M construct(ColumnData<M> dataMap, ObjectSource objectSource);
}
