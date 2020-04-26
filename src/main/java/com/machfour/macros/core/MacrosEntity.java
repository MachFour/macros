package com.machfour.macros.core;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static java.lang.Double.NaN;

/**
 * Defines common methods for each object to be persisted
 */

public interface MacrosEntity<M extends MacrosEntity<M>> {

    long NO_ID = -100;
    // special ID for the 'null' serving of just grams / mL
    long UNIT_SERVING = -101;
    long NO_DATE = -99;
    double UNSET = NaN;

    @NotNull
    Long getId();

    default boolean hasId() {
        return getId() != NO_ID;
    }

    @NotNull
    Long createTime();

    @NotNull
    Long modifyTime();

    @NotNull
    ObjectSource getObjectSource();

    // Used to get data by column objects
    <J> J getData(Column<M, J> c);

    boolean hasData(Column<M, ?> c);

    // Creates a mapping of column objects to their values for this instance
    ColumnData<M> getAllData(boolean readOnly);
    // equivalent to getAllData(true)
    ColumnData<M> getAllData();

    Table<M> getTable();
    Factory<M> getFactory();

    // ... Alternative methods that can be used with unique columns
    <N extends MacrosEntity<N>, J> void setFkParentNaturalKey(Column.Fk<M, ?, N> fkCol, Column<N, J> parentNaturalKey, N parent);
    <N, J> void setFkParentNaturalKey(Column.Fk<M, ?, N> fkCol, Column<N, J> parentNaturalKey, J data);
    <N> ColumnData<N> getFkParentNaturalKey(Column.Fk<M, ?, N> fkCol);
    Map<Column.Fk<M, ?, ?>, ?> getFkNaturalKeyMap();
    void copyFkNaturalKeyMap(MacrosEntity<M> from);
}
