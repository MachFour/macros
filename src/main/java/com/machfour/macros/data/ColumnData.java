package com.machfour.macros.data;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.*;

// Class which maps columns to their data values in instances of Macros objects
public class ColumnData<M> {
    // internally, since all of the columns are known at compile time, we can just assign an index to each one
    // and store the values in a list according to that index.
    private final Table<M> table;
    private final List<Object> data;
    private final List<Boolean> hasData;

    @Override
    public boolean equals(Object o) {
        return o instanceof ColumnData
                && table.equals(((ColumnData) o).table)
                // this should be implied by the equality below
                //&& hasData.equals(((ColumnData) o).hasData)
                && data.equals(((ColumnData) o).data);
    }

    public static <M> boolean columnsAreEqual(ColumnData<M> c1, ColumnData<M> c2, List<Column<M, ?>> columnsToCheck) {
        if (c1 == null || c2 == null) {
            return false;
        }
        for (Column<M, ?> col: columnsToCheck) {
            if (!Objects.equals(c1.unboxColumn(col), c2.unboxColumn(col))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, data);
    }

    @Override
    public String toString() {
        return "ColumnData<" + table.name() + "> : " + data.toString();
    }

    protected ColumnData(Table<M> table, List<Column<M, ?>> columns, @Nullable ColumnData<M> existing, boolean immutable) {
        List<Object> data = new ArrayList<>(columns.size());
        List<Boolean> hasData = new ArrayList<>(columns.size());
        // prefill lists with null so that we can set any index
        for (int i = 0; i < columns.size(); i++) {
            data.add(null);
            hasData.add(false);
        }
        // initialise to defaults
        for (Column<M, ?> col : columns) {
            // can't use the putData() method due to type erasure
            Object initialData = (existing == null) ? col.defaultData() : existing.unboxColumn(col);
            data.set(col.index(), initialData);
            hasData.set(col.index(), initialData != null);
        }
        this.table = table;
        this.data = immutable ? Collections.unmodifiableList(data) : data;
        this.hasData = immutable ? Collections.unmodifiableList(hasData) : hasData;
    }

    public ColumnData(@NotNull Table<M> t) {
        this(t, t.columns(), null, false);
    }

    public ColumnData(@NotNull ColumnData<M> existing) {
        this(existing.table, existing.table.columns(), existing, false);
    }

    public ColumnData(@NotNull ColumnData<M> existing, boolean isImmutableCopy) {
        this(existing.table, existing.table.columns(), existing, isImmutableCopy);
    }

    // columns are the same so there will be no type issues
    public static <M> void copyData(@NotNull ColumnData<M> from, @NotNull ColumnData<M> to, List<Column<M, ?>> whichCols) {
        for (Column<M, ?> col : whichCols) {
            // can't use the putData() method due to type erasure
            Object o = from.data.get(col.index());
            to.data.set(col.index(), o);
            to.hasData.set(col.index(), o != null);
        }
    }

    public Table<M> getTable() {
        return table;
    }

    // the type of the data is ensured at time of adding it to this columnData object.
    public <J> J unboxColumn(@NotNull Column<M, J> col) {
        return col.javaClass().cast(data.get(col.index()));
    }

    // will throw exception if the data doesn't match the type
    public <J> void putData(@NotNull Column<M, J> col, J data) {
        this.data.set(col.index(), data);
        hasData.set(col.index(), data != null);
    }

    public boolean hasData(Column<M, ?> col) {
        return hasData.get(col.index());
    }
}
