package com.machfour.macros.objects;

import com.machfour.macros.core.*;

public class FoodAttribute extends MacrosEntity<FoodAttribute> {

    private FoodAttribute(ColumnData<FoodAttribute> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<FoodAttribute> getTable() {
        return Schema.FoodAttributeTable.instance();
    }

    public static Factory<FoodAttribute> factory() {
        return FoodAttribute::new;
    }
    @Override
    public Factory<FoodAttribute> getFactory() {
        return factory();
    }

}
