package com.machfour.macros.data;

import com.sun.istack.internal.NotNull;

import java.util.*;

// Class which maps columns to their data values in instances of Macros objects
public class ColumnData<M> {

    private final Map<Column<M, ?, ?>, DataContainer<?, ?>> map;
    private final Map<Column<M, ?, ?>, Boolean> hasData;
    private final Table<M> table;

    @Override
    public boolean equals(Object o) {
        return o instanceof ColumnData
                && table.equals(((ColumnData) o).table)
                // this should be implied by the equality below
                //&& hasData.equals(((ColumnData) o).hasData)
                && map.equals(((ColumnData) o).map);

    }

    public static <M> boolean columnsAreEqual(ColumnData<M> c1, ColumnData<M> c2, List<Column<M, ?, ?>> columnsToCheck) {
        if (c1 == null || c2 == null) {
            return false;
        }
        boolean equal = true;
        for (Column<M, ?, ?> col: columnsToCheck) {
            if (!Objects.equals(c1.unboxColumn(col), c2.unboxColumn(col))) {
                equal = false;
            }
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, map);
    }

    @Override
    public String toString() {
        return table.name() + " data: " + map.toString();
    }

    public ColumnData(@NotNull Table<M> t) {
        table = t;
        map = new HashMap<>(t.columns().size(), 1);
        hasData = new HashMap<>(t.columns().size());

        for (Column<M, ?, ?> c : t.columns()) {
            map.put(c, new DataContainer<>(c.type(), null));
            hasData.put(c, false);
        }
    }

    public ColumnData(@NotNull ColumnData<M> existing) {
        this(existing, false);
    }

    public ColumnData(@NotNull ColumnData<M> existing, boolean isImmutableCopy) {
        table = existing.table;
        if (isImmutableCopy) {
            map = Collections.unmodifiableMap(existing.map);
            hasData = Collections.unmodifiableMap(existing.hasData);
        } else {
            map = new HashMap<>(table.columns().size(), 1);
            hasData = new HashMap<>(table.columns().size());
            map.putAll(existing.map);
            hasData.putAll(existing.hasData);
        }
    }

    // columns are the same so there will be no type issues
    public static <M> void copyData(@NotNull ColumnData<M> from, @NotNull ColumnData<M> to) {
        for (Column<M, ?, ?> col : from.table.columns()) {
            DataContainer<?, ?> dc = from.map.get(col);
            to.map.put(col, dc.clone());
        }
    }

    public Table<M> getTable() {
        return table;
    }

    // the type of the data is ensured at time of DataContainer creation.
    @SuppressWarnings("unchecked")
    public <T extends MacrosType<J>, J> J unboxColumn(@NotNull Column<M, T, J> col) {
        DataContainer<?, ?> dc = map.get(col);
        return (J) dc.getData();
    }

    // will throw exception if the data doesn't match the type
    public <T extends MacrosType<J>, J> void putData(Column<M, T, J> col, J data) {
        map.put(col, new DataContainer<>(col.type(), data));
        hasData.put(col, data != null);
    }

    public boolean hasData(Column<M, ?, ?> col) {
        return hasData.get(col);
    }
}
