package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Meal extends MacrosEntity<Meal> {

    private final List<FoodPortion> foodPortions;

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

    public static Factory<Meal> factory() {
        return Meal::new;
    }
    @Override
    public Factory<Meal> getFactory() {
        return factory();
    }
    @Override
    public Table<Meal> getTable() {
        return table();
    }
    public static Table<Meal> table() {
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

    public String getName() {
        return getData(Schema.MealTable.NAME);
    }

    public DateStamp getDay() {
        return getData(Schema.MealTable.DAY);
    }

    public List<FoodPortion> getFoodPortions() {
        return new ArrayList<>(foodPortions);
    }

    public void addFoodPortion(@NotNull FoodPortion fp) {
        assert !foodPortions.contains(fp) && foreignKeyMatches(fp, Schema.FoodPortionTable.MEAL_ID, this);
        foodPortions.add(fp);
    }
}
