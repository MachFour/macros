package com.machfour.macros.data;

import com.machfour.macros.core.MacrosPersistable;
import com.sun.istack.internal.NotNull;

import java.util.HashMap;
import java.util.Map;

// Class which maps columns to their data values in instances of Macros objects
public class ColumnData<M extends MacrosPersistable> {

    private final Map<Column<M, ?>, DataContainer<?>> map;
    private final Map<Column<M, ?>, Boolean> hasData;
    private final Table<M> table;

    public ColumnData(@NotNull Table<M> t) {
        table = t;
        map = new HashMap<>(t.columns().size(), 1);
        hasData = new HashMap<>(t.columns().size());

        for (Column<M, ?> c : t.columns()) {
            map.put(c, new DataContainer<>(c.type(), null));
            hasData.put(c, false);
        }
    }

    public ColumnData(@NotNull ColumnData<M> existing) {
        this(existing.table);
        map.putAll(existing.map);
        hasData.putAll(existing.hasData);
    }

    public Table<M> getTable() {
        return table;
    }

    public <T> T unboxColumn(@NotNull Column<M, T> col) {
        assert map.containsKey(col) : "Invalid column for table";
        DataContainer dc = map.get(col);
        return col.type().javaClass().cast(dc.getData());
    }

    // will throw exception if the data doesn't match the type
    public <T> void putData(Column<M, T> col, T data) {
        assert map.containsKey(col) : "Invalid column for table";
        map.put(col, new DataContainer<>(col.type(), data));
        hasData.put(col, data != null);
    }

    public <T> void putDataUnchecked(Column<M, ?> col, Object data, MacrosType<T> type) {
        assert col.type().equals(type);
        putData((Column<M, T>) col, (T) data);
    }

    public boolean hasData(Column<M, ?> col) {
        assert map.containsKey(col) : "Invalid column for table";
        return hasData.get(col);
    }
}
