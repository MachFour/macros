package com.machfour.macros.objects;

import com.machfour.macros.core.*;

public class RegularMeal extends MacrosEntity<RegularMeal> {

    public RegularMeal(ColumnData<RegularMeal> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<RegularMeal> getTable() {
        return Schema.RegularMealTable.instance();
    }

    public static Factory<RegularMeal> factory() {
        return RegularMeal::new;
    }
    @Override
    public Factory<RegularMeal> getFactory() {
        return factory();
    }
}
