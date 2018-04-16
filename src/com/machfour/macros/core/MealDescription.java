package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;

public class MealDescription extends MacrosEntity<MealDescription> {

    public MealDescription(ColumnData<MealDescription> data, boolean isFromDb) {
        super(data, isFromDb);
    }

    @Override
    public Table<MealDescription> getTable() {
        return Tables.MealDescriptionTable.getInstance();
    }

}
