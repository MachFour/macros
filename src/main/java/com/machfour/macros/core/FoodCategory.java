package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Schema;

public class FoodCategory extends MacrosEntity<FoodCategory> {

    public FoodCategory(ColumnData<FoodCategory> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<FoodCategory> getTable() {
        return Schema.FoodCategoryTable.instance();
    }

    public String getName() {
        return getTypedDataForColumn(Schema.FoodCategoryTable.NAME);
    }
}
