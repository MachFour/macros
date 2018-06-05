package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Schema;

public class RegularMeal extends MacrosEntity<RegularMeal> {

    public RegularMeal(ColumnData<RegularMeal> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<RegularMeal> getTable() {
        return Schema.RegularMealTable.instance();
    }

}
