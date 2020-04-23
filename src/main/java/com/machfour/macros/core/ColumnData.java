package com.machfour.macros.core;

import com.machfour.macros.core.datatype.TypeCastException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

// Class which maps columns to their data values in instances of Macros objects
public class ColumnData<M>  {
    // internally, since all of the columns are known at compile time, we can just assign an index to each one
    // and store the values in a list according to that index.
    private final Table<M> table;
    private final Object[] data;
    private final boolean[] hasData;
    // which columns have data stored in this ColumnData object;
    private final Set<Column<M, ?>> columns;

    private boolean immutable;

    public final <J> void putFromNullableString(Column<M, J> col, @Nullable String data) throws TypeCastException {
        // also catch empty whitespace with trim()
        if (data == null || data.trim().equals("")) {
            putFromRaw(col, null);
        } else {
            putFromString(col, data);
        }
    }

    // null represented by empty string
    @NotNull
    public final <J> String getAsRawString(Column<M, J> col) {
        return col.getType().toRawString(get(col));
    }
    // null represented by "NULL"
    @NotNull
    public final <J> String getAsSqlString(Column<M, J> col) {
        return col.getType().toSqlString(get(col));
    }

    public final <J> void putFromString(Column<M, J> col, @NotNull String data) throws TypeCastException {
        put(col, col.getType().fromString(data));
    }

    public final <J> Object getAsRaw(Column<M, J> col) {
        return col.getType().toRaw(get(col));
    }

    public final <J> void putFromRaw(Column<M, J> col, Object data) throws TypeCastException {
        put(col, col.getType().fromRaw(data));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColumnData<?>)) {
            return false;
        }
        ColumnData<?> otherCd = (ColumnData<?>) o;
        return table.equals(otherCd.table) && Arrays.deepEquals(data, otherCd.data);
        // the following equality should be implied by the latter one above:
        // hasData.equals(((ColumnData) o).hasData)
    }

    public final boolean hasColumns(@NotNull Collection<Column<M, ?>> cols) {
        return getColumns().containsAll(cols);
    }

    public final Set<Column<M, ?>> getColumns() {
        return columns;
    }

    public static <M> boolean columnsAreEqual(ColumnData<M> c1, ColumnData<M> c2, List<Column<M, ?>> whichCols) {
        if (c1 == null || c2 == null || !c1.hasColumns(whichCols) || !c1.hasColumns(whichCols)) {
            return false;
        }
        for (Column<M, ?> col: whichCols) {
            // TODO if (!Objects.equals(c1.get(col), c2.get(col))) {
            Object c1Data = c1.get(col);
            Object c2Data = c2.get(col);
            if (c1Data != null && !c1Data.equals(c2Data)) {
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
        tempCols.addAll(cols);
        columns = Collections.unmodifiableSet(tempCols);

        setDefaultData();
        if (existing != null) {
            copyData(existing, this, cols);
        }
    }

    final void setImmutable() {
        this.immutable = true;
    }

    public ColumnData(Table<M> t, List<Column<M, ?>> cols) {
        this(t, cols, null);
    }

    public ColumnData(@NotNull Table<M> t) {
        this(t, t.columns(), null);
    }

    public final boolean isImmutable() {
        return immutable;
    }


    public final void setDefaultData() {
        setDefaultData(columns);
    }

    public final void setDefaultData(Collection<Column<M, ?>> cols) {
        assert hasColumns(cols);
        assertMutable();
        for (Column<M, ?> col : cols) {
            Object o = col.defaultData();
            // can't use the put() method due to type erasure
            data[col.index()] = o;
            hasData[col.index()] = (o != null);
        }

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
        assert !isImmutable() : "ColumnData has been made immutable";
    }

    public Table<M> getTable() {
        return table;
    }

    // the type of the data is ensured at time of adding it to this columnData object.
    @Nullable
    public final <J> J get(@NotNull Column<M, J> col) {
        assertHasColumn(col);
        return getWithoutAssert(col);
    }

    @Nullable
    private <J> J getWithoutAssert(@NotNull Column<M, J> col) {
        return col.getType().cast(data[col.index()]);
    }

    // will throw exception if the column is not present
    // No validation is performed on the value
    public final <J> void put(@NotNull Column<M, J> col, J value) {
        assertHasColumn(col);
        assertMutable();
        data[col.index()] = value;
        hasData[col.index()] = (value != null);
    }

    public final boolean hasData(Column<M, ?> col) {
        assertHasColumn(col);
        return hasData[col.index()];
    }
}
