package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Schema;

public class FoodAttribute extends MacrosEntity<FoodAttribute> {

    public FoodAttribute(ColumnData<FoodAttribute> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<FoodAttribute> getTable() {
        return Schema.FoodAttributeTable.instance();
    }

}
