package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;

public class FoodAttribute extends MacrosEntity<FoodAttribute> {

    public FoodAttribute(ColumnData<FoodAttribute> data, boolean isFromDb) {
        super(data, isFromDb);
    }

    @Override
    public Table<FoodAttribute> getTable() {
        return Tables.FoodAttributeTable.getInstance();
    }

}
