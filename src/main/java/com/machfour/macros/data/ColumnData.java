package com.machfour.macros.data;

import com.sun.istack.internal.NotNull;

import java.util.*;

// Class which maps columns to their data values in instances of Macros objects
public class ColumnData<M> {

    // internally, since all of the columns are known at compile time, we can just assign an index to each one
    // and store the values in a list according to that index.
    private final List<Object> data;
    private final List<Boolean> hasData;
    private final Table<M> table;

    @Override
    public boolean equals(Object o) {
        return o instanceof ColumnData
                && table.equals(((ColumnData) o).table)
                // this should be implied by the equality below
                //&& hasData.equals(((ColumnData) o).hasData)
                && data.equals(((ColumnData) o).data);

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
        return Objects.hash(table, data);
    }

    @Override
    public String toString() {
        return table.name() + " data: " + data.toString();
    }

    public ColumnData(@NotNull Table<M> t) {
        table = t;
        int newSize = t.columns().size();
        data = new ArrayList<>(newSize);
        hasData = new ArrayList<>(newSize);

        // it's important to preserve the order of the columns as they were passed to the Table object
        // otherwise we'll get an OutOfBoundsException because we're adding beyond the end of the list.

        // alternatively - we could fill both lists with null first.
        for (int i = 0; i < newSize; i++) {
            data.add(null);
            hasData.add(false);
        }
    }

    public ColumnData(@NotNull ColumnData<M> existing) {
        this(existing, false);
    }

    public ColumnData(@NotNull ColumnData<M> existing, boolean isImmutableCopy) {
        table = existing.table;
        if (isImmutableCopy) {
            data = Collections.unmodifiableList(existing.data);
            hasData = Collections.unmodifiableList(existing.hasData);
        } else {
            int newSize = table.columns().size();
            data = new ArrayList<>(newSize);
            hasData = new ArrayList<>(newSize);
            for (int i = 0; i < newSize; i++) {
                data.add(i, existing.data.get(i));
                hasData.add(i, existing.hasData.get(i));
            }
        }
    }

    // columns are the same so there will be no type issues
    public static <M> void copyData(@NotNull ColumnData<M> from, @NotNull ColumnData<M> to) {
        for (Column<M, ?, ?> col : from.table.columns()) {
            Object o = from.data.get(col.index());
            to.data.set(col.index(), o);
            to.hasData.set(col.index(), o != null);
        }
    }

    public Table<M> getTable() {
        return table;
    }

    // the type of the data is ensured at time of adding it to this columnData object.
    public <T extends MacrosType<J>, J> J unboxColumn(@NotNull Column<M, T, J> col) {
        Class<J> typeClass = col.type().javaClass();
        return typeClass.cast(data.get(col.index()));
    }

    // will throw exception if the data doesn't match the type
    public <T extends MacrosType<J>, J> void putData(Column<M, T, J> col, J data) {
        this.data.set(col.index(), data);
        hasData.set(col.index(), data != null);
    }

    public boolean hasData(Column<M, ?, ?> col) {
        return hasData.get(col.index());
    }
}
