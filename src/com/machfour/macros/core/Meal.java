package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Columns;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;
import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Meal extends MacrosEntity<Meal> {

    //public static final MacrosPersistable.Converter<MealTable> CONVERTER = Converter.getInstance();
    private final List<FoodPortion> foodPortions;

    public Meal(ColumnData<Meal> data, boolean isFromDb) {
        super(data, isFromDb);
        foodPortions = new ArrayList<>();
    }

    public static NutritionData sumNutritionTotals(List<Meal> meals) {
        List<NutritionData> nutritionTotals = new ArrayList<>(meals.size());
        for (Meal m : meals) {
            nutritionTotals.add(m.getNutritionTotal());
        }
        return NutritionData.sum(nutritionTotals);
    }

    @Override
    public Table<Meal> getTable() {
        return Tables.MealTable.getInstance();
    }

    public NutritionData getNutritionTotal() {
        List<NutritionData> nutritionComponents = new ArrayList<>(foodPortions.size());
        for (FoodPortion fp : foodPortions) {
            nutritionComponents.add(fp.getNutritionData());
        }
        return NutritionData.sum(nutritionComponents);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Meal && super.equals(o);
    }

    @Nullable
    public String getDescription() {
        return getTypedDataForColumn(Columns.MealCol.DESCRIPTION);
    }

    public DateStamp getDay() {
        return getTypedDataForColumn(Columns.MealCol.DAY);
    }

    public List<FoodPortion> getFoodPortions() {
        return new ArrayList<>(foodPortions);
    }

    public void addFoodPortion(@NotNull FoodPortion fp) {
        assert (getId().equals(fp.getMealId()));
        assert (equals(fp.getMeal()));
        assert (!foodPortions.contains(fp));
        foodPortions.add(fp);
    }
}
