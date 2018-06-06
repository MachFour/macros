package com.machfour.macros.data;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.*;

// Class which maps columns to their data values in instances of Macros objects
public class ColumnData<M> {
    // internally, since all of the columns are known at compile time, we can just assign an index to each one
    // and store the values in a list according to that index.
    private final Table<M> table;
    private final Object[] data;
    private final boolean[] hasData;
    private final boolean immutable;
    // which columns have data stored in this ColumnData object;
    private final Set<Column<M, ?>> columns;

    @Override
    public boolean equals(Object o) {
        return o instanceof ColumnData
                && table.equals(((ColumnData) o).table)
                // this should be implied by the equality below
                //&& hasData.equals(((ColumnData) o).hasData)
                && Arrays.deepEquals(data, ((ColumnData) o).data);
    }

    public boolean hasColumns(@NotNull List<Column<M, ?>> cols) {
        return getColumns().containsAll(cols);
    }

    public boolean hasColumn(@NotNull Column<M, ?> col) {
        return getColumns().contains(col);
    }

    public Set<Column<M, ?>> getColumns() {
        return columns;
    }

    public static <M> boolean columnsAreEqual(ColumnData<M> c1, ColumnData<M> c2, List<Column<M, ?>> whichCols) {
        if (c1 == null || c2 == null || !c1.hasColumns(whichCols) || !c1.hasColumns(whichCols)) {
            return false;
        }
        for (Column<M, ?> col: whichCols) {
            if (!Objects.equals(c1.get(col), c2.get(col))) {
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
        return "ColumnData<" + table.name() + "> : " + Arrays.deepToString(data);
    }

    protected ColumnData(Table<M> table, List<Column<M, ?>> cols, @Nullable ColumnData<M> existing, boolean immutable) {
        // in order to have an arbitrary set of table columns used, we need to have an arraylist big enough to
        // hold columns of any index, up to the number of columns in the table minus 1.
        int arraySize = table.columns().size();
        this.data = new Object[arraySize];
        this.hasData = new boolean[arraySize];
        this.table = table;
        this.immutable = immutable;
        HashSet<Column<M, ?>> tempCols = new HashSet<>(cols.size(), 1);
        // initialise to defaults
        for (Column<M, ?> col : cols) {
            // can't use the put() method due to type erasure
            Object initialData = (existing == null) ? col.defaultData() : existing.get(col);
            data[col.index()] = initialData;
            hasData[col.index()] = (initialData != null);
            tempCols.add(col);
        }
        columns = Collections.unmodifiableSet(tempCols);
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

    public boolean isImmutable() {
        return immutable;
    }

    // columns are the same so there will be no type issues
    public static <M> void copyData(@NotNull ColumnData<M> from, @NotNull ColumnData<M> to, List<Column<M, ?>> which) {
        assert to.hasColumns(which) && from.hasColumns(which): "Specified columns not present in both from and to";
        to.assertMutable();
        for (Column<M, ?> col : which) {
            Object o = from.data[col.index()];
            to.data[col.index()] = o;
            to.hasData[col.index()] = (o != null);
        }
    }

    private void assertHasColumn(Column<M, ?> col) {
        assert hasColumn(col) : "Column " + col + " not present";
    }
    private void assertMutable() {
        assert !isImmutable() : getClass().toString() + " is immutable";
    }

    public Table<M> getTable() {
        return table;
    }

    // the type of the data is ensured at time of adding it to this columnData object.
    public <J> J get(@NotNull Column<M, J> col) {
        assertHasColumn(col);
        return col.javaClass().cast(data[col.index()]);
    }

    // will throw exception if the data doesn't match the type
    public <J> void put(@NotNull Column<M, J> col, J d) {
        assertHasColumn(col);
        assertMutable();
        data[col.index()] = d;
        hasData[col.index()] = (d != null);
    }

    public boolean hasData(Column<M, ?> col) {
        assertHasColumn(col);
        assertMutable();
        return hasData[col.index()];
    }
}
