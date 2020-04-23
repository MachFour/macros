package com.machfour.macros.objects;

import com.machfour.macros.core.*;

public class FoodCategory extends MacrosEntityImpl<FoodCategory> {

    public FoodCategory(ColumnData<FoodCategory> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Table<FoodCategory> getTable() {
        return table();
    }

    public String getName() {
        return getData(Schema.FoodCategoryTable.NAME);
    }

    public static Factory<FoodCategory> factory() {
        return FoodCategory::new;
    }

    public static Table<FoodCategory> table() {
        return Schema.FoodCategoryTable.instance();
    }
    @Override
    public Factory<FoodCategory> getFactory() {
        return factory();
    }
}
