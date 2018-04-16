package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;

public class AttributeMap extends MacrosEntity<AttributeMap> {

    public AttributeMap(ColumnData<AttributeMap> data, boolean isFromDb) {
        super(data, isFromDb);
    }

    @Override
    public Table<AttributeMap> getTable() {
        return Tables.AttributeMapTable.getInstance();
    }

}
