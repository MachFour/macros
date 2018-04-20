package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;

public class RegularMeal extends MacrosEntity<RegularMeal> {

    public RegularMeal(ColumnData<RegularMeal> data, boolean isFromDb) {
        super(data, isFromDb);
    }

    @Override
    public Table<RegularMeal> getTable() {
        return Tables.RegularMealTable.instance();
    }

}
