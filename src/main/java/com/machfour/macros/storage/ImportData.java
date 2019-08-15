package com.machfour.macros.storage;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.Table;

import java.util.HashMap;
import java.util.Map;

// wrapper class around ColumnData to use for importing, in cases where
// extra information needs to be stored with the imported row data that
// is the wrong type for the actual column
// Present use case: storing food index names instead of IDs when importing
public class ImportData<M> extends ColumnData<M> {
    private final Map<Column<M, ?>, String> extraData;

    public ImportData(Table<M> table) {
        super(table);
        this.extraData = new HashMap<>();
    }

    public void putExtraData(Column<M, ?> col, String value) {
        extraData.put(col, value);
    }

    public String getExtraData(Column<M, ?> col) {
        return extraData.get(col);
    }

}
