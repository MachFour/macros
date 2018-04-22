package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;

public class FoodCategory extends MacrosEntity<FoodCategory> {

    public FoodCategory(ColumnData<FoodCategory> data, boolean isFromDb) {
        super(data, isFromDb);
    }

    @Override
    public Table<FoodCategory> getTable() {
        return Tables.FoodCategoryTable.instance();
    }

}
