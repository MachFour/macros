package com.machfour.macros.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// Class which maps columns to their data values in instances of Macros objects
public final class ColumnData<M> {
    // internally, since all of the columns are known at compile time, we can just assign an index to each one
    // and store the values in a list according to that index.
    private final Table<M> table;
    private final Object[] data;
    private final boolean[] hasData;
    // which columns have data stored in this ColumnData object;
    private final Set<Column<M, ?>> columns;

    private boolean immutable;

    public <J> void putFromNullableString(Column<M, J> col, @Nullable String data) {
        // also catch empty whitespace with trim()
        if (data == null || data.trim().equals("")) {
            putFromRaw(col, null);
        } else {
            putFromString(col, data);
        }
    }

    // null represented by empty string
    @NotNull
    public <J> String getAsString(Column<M, J> col) {
        return col.getType().toRawString(get(col));
    }
    // null represented by "NULL"
    @NotNull
    public <J> String getAsSqlString(Column<M, J> col) {
        return col.getType().toSqlString(get(col));
    }

    public <J> void putFromString(Column<M, J> col, @NotNull String data) {
        put(col, col.getType().fromString(data));
    }

    public <J> Object getAsRaw(Column<M, J> col) {
        return col.getType().toRaw(get(col));
    }

    public <J> void putFromRaw(Column<M, J> col, Object data) {
        put(col, col.getType().fromRaw(data));
    }

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

    public Set<Column<M, ?>> getColumns() {
        return columns;
    }

    public static <M> boolean columnsAreEqual(ColumnData<M> c1, ColumnData<M> c2, List<Column<M, ?>> whichCols) {
        if (c1 == null || c2 == null || !c1.hasColumns(whichCols) || !c1.hasColumns(whichCols)) {
            return false;
        }
        for (Column<M, ?> col: whichCols) {
            // TODO if (!Objects.equals(c1.get(col), c2.get(col))) {
            if (c1.get(col) != null && c1.get(col).equals(c2.get(col))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        // TODO return Objects.hash(table, data);
        return Arrays.hashCode(new Object[]{table, data});
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("ColumnData<").append(table.name()).append("> [");
        for (Column<M, ?> col: columns) {
            // TODO replace with Objects.toString() when Android API level can be bumped
            str.append(String.format("%s = %s, ", col.sqlName(), data[col.index()]));
        }
        str.append("]");
        return str.toString();
    }

    // Caller (other constructors in this class) must ensure: existing.hasColumns(cols)
    private ColumnData(Table<M> table, List<Column<M, ?>> cols, @Nullable ColumnData<M> existing) {
        // in order to have an arbitrary set of table columns used, we need to have an arraylist big enough to
        // hold columns of any index, up to the number of columns in the table minus 1.
        int arraySize = table.columns().size();
        this.data = new Object[arraySize];
        this.hasData = new boolean[arraySize];
        this.table = table;
        this.immutable = false;
        HashSet<Column<M, ?>> tempCols = new HashSet<>(cols.size(), 1);
        // initialise to defaults
        for (Column<M, ?> col : cols) {
            // can't use the put() method due to type erasure
            Object initialData = existing == null ? col.defaultData() : existing.getWithoutAssert(col);
            data[col.index()] = initialData;
            hasData[col.index()] = (initialData != null);
            tempCols.add(col);
        }
        columns = Collections.unmodifiableSet(tempCols);
        if (existing != null) {
            copyData(existing, this, cols);
        }
    }

    public void setImmutable() {
        this.immutable = true;
    }

    public ColumnData(Table<M> t, List<Column<M, ?>> cols) {
        this(t, cols, null);
    }

    public ColumnData(@NotNull Table<M> t) {
        this(t, t.columns(), null);
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

    public ColumnData<M> copy() {
        return new ColumnData<>(table, table.columns(), this);
    }

    public ColumnData<M> copy(List<Column<M, ?>> whichCols) {
        assertHasColumns(whichCols);
        return new ColumnData<>(table, whichCols, this);
    }

    private void assertHasColumn(Column<M, ?> col) {
        assertHasColumns(Collections.singletonList(col));
    }
    private void assertHasColumns(List<Column<M, ?>> cols) {
        assert columns.containsAll(cols);
    }
    private void assertMutable() {
        assert !isImmutable() : "ColumnData is immutable";
    }

    public Table<M> getTable() {
        return table;
    }

    // the type of the data is ensured at time of adding it to this columnData object.
    public <J> J get(@NotNull Column<M, J> col) {
        assertHasColumn(col);
        return getWithoutAssert(col);
    }
    private <J> J getWithoutAssert(@NotNull Column<M, J> col) {
        return col.getType().cast(data[col.index()]);
    }

    // will throw exception if the column is not present
    public <J> void put(@NotNull Column<M, J> col, J value) {
        assertHasColumn(col);
        assertMutable();
        data[col.index()] = value;
        hasData[col.index()] = (value != null);
    }

    public boolean hasData(Column<M, ?> col) {
        assertHasColumn(col);
        return hasData[col.index()];
    }
}
