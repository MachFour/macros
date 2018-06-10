package com.machfour.macros.core;

import java.util.List;
import java.util.Map;

public interface Table<M> {
    String name();

    List<Column<M, ?>> columns();
    // return all FK columns
    List<Column.Fk<M, ?, ?>> fkColumns();

    Map<String, Column<M, ?>> columnsByName();

    Column<M, ?> columnForName(String name);

    Column<M, Long> getIdColumn();

    Column<M, Long> getCreateTimeColumn();

    Column<M, Long> getModifyTimeColumn();

    // returns a list of columns that can be used to identify an individual row,
    // if such a list exists for this table. If not, an empty list is returned.
    List<Column<M, ?>> getSecondaryKeyCols();
    // special case when secondary key has a single column.
    Column<M, ?> getNaturalKeyColumn();

    Factory<M> getFactory();

    default M construct(ColumnData<M> dataMap, ObjectSource objectSource) {
        return getFactory().construct(dataMap, objectSource);
    }
}
