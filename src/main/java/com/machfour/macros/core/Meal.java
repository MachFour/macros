package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Schema;
import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Meal extends MacrosEntity<Meal> {

    private final List<FoodPortion> foodPortions;
    private MealDescription mealDescription;

    public Meal(ColumnData<Meal> data, ObjectSource objectSource) {
        super(data, objectSource);
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
        return Schema.MealTable.instance();
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
    public MealDescription getDescription() {
        return mealDescription;
    }

    public DateStamp getDay() {
        return getTypedDataForColumn(Schema.MealTable.DAY);
    }

    public List<FoodPortion> getFoodPortions() {
        return new ArrayList<>(foodPortions);
    }

    public void addFoodPortion(@NotNull FoodPortion fp) {
        assert !foodPortions.contains(fp) && foreignKeyMatches(fp, Schema.FoodPortionTable.MEAL_ID, this);
        foodPortions.add(fp);
    }

    public void setMealDescription(@NotNull MealDescription md) {
        assert mealDescription == null && foreignKeyMatches(this, Schema.MealTable.DESCRIPTION, md);
        mealDescription = md;
    }

}
