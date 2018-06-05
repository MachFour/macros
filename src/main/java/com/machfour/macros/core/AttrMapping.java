package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Schema;

public class AttrMapping extends MacrosEntity<AttrMapping> {

    public AttrMapping(ColumnData<AttrMapping> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<AttrMapping> getTable() {
        return Schema.AttrMappingTable.instance();
    }

}
