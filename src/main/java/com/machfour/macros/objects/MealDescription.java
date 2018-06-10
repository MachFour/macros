package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;

public class MealDescription extends MacrosEntity<MealDescription> {

    private MealDescription(ColumnData<MealDescription> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @NotNull
    public String getName() {
        return getData(Schema.MealDescriptionTable.NAME);
    }

    public static Table<MealDescription> table() {
        return Schema.MealDescriptionTable.instance();
    }
    @Override
    public Table<MealDescription> getTable() {
        return table();
    }

    public static Factory<MealDescription> factory() {
        return MealDescription::new;
    }
    @Override
    public Factory<MealDescription> getFactory() {
        return factory();
    }
}
