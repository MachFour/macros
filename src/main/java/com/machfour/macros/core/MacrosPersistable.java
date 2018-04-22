package com.machfour.macros.core;

import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;

import java.util.List;
import java.util.Map;

import static java.lang.Double.NaN;

/**
 * Defines common methods for each object to be persisted
 */

public interface MacrosPersistable<M extends MacrosPersistable> {

    long NO_ID = -100;
    // special ID for the 'null' serving of just grams / mL
    long METRIC_SERVING = -101;
    long NO_DATE = -99;
    double UNSET = NaN;

    Long getId();

    Long getCreateTime();

    Long getModifyTime();

    boolean isFromDb();

    // Used to access the columns defined in the corresponding table schema
    List<Column<M, ?>> getColumns();

    Map<String, Column<M, ?>> getColumnsByName();

    // Used to get data by column objects
    <T> T getTypedDataForColumn(Column<M, T> c);

    boolean hasData(Column<M, ?> c);

    // Creates a mapping of column objects to their values for this instance
    ColumnData<M> getAllData();

    Table<M> getTable();
}
