package com.machfour.macros.core;

import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.sun.istack.internal.NotNull;

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

    @NotNull
    Long getId();

    default boolean hasId() {
        return getId() != NO_ID;
    }

    @NotNull
    Long getCreateTime();

    @NotNull
    Long getModifyTime();

    @NotNull
    ObjectSource getObjectSource();

    // Used to get data by column objects
    <J> J getData(Column<M, J> c);

    boolean hasData(Column<M, ?> c);

    // Creates a mapping of column objects to their values for this instance
    ColumnData<M> getAllData();

    Table<M> getTable();

    // used to set Secondary FK data when an object is available
    <N extends MacrosPersistable<N>> void setFkParentBy2aryKey(Column.Fk<M, Long, N> col, N parent);
    // ... or when only the relevant column data is available, but then it's only limited to single-column secondary keys
    <N extends MacrosPersistable<N>, J> void setFkParentBy2aryKey(Column.Fk<M, Long, N> col, Table<N> parentTable, Column<N, J> parent2aryKey, J data);

    <N extends MacrosPersistable<N>> ColumnData<N> getFkParent2aryData(Column.Fk<M, Long, N> col);

    Map<Column.Fk<M, Long, ?>, ColumnData<?>> getSecondaryFkMap();
}
