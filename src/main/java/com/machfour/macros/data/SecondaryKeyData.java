package com.machfour.macros.data;

import com.machfour.macros.core.MacrosPersistable;
import com.sun.istack.internal.NotNull;

public class SecondaryKeyData<M extends MacrosPersistable<M>> {
    private final ColumnData<M> data;

    public SecondaryKeyData(Table<M> table) {
        data = new ColumnData<>(table, table.getSecondaryKey(), null, false);
        assert !table.getSecondaryKey().isEmpty() : "Table " + table.name() + " has no secondary key columns defined";
    }

    public void setFromObject(@NotNull M object) {
        for (Column<M, ?> col : data.getColumns()) {
            setColumnFromObject(object, col);
        }
    }
    // Generic capture helper (can't inline this)
    private <J> void setColumnFromObject(M object, Column<M, J> col) {
        data.put(col, object.getTypedDataForColumn(col));
    }

}
