package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Schema;

public class MealDescription extends MacrosEntity<MealDescription> {

    public MealDescription(ColumnData<MealDescription> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<MealDescription> getTable() {
        return Schema.MealDescriptionTable.instance();
    }

}
