package com.machfour.macros.core;

import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.sun.istack.internal.NotNull;

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

    <N extends MacrosPersistable<N>> void setSecondaryFkParent(Column.ForeignKey<M, Long, N> col, N parent);

    <N extends MacrosPersistable<N>> ColumnData<N> getSecondaryFkData(Column.ForeignKey<M, Long, N> col);

    Map<Column.ForeignKey<M, Long, ?>, ColumnData<?>> getSecondaryFkMap();
}
