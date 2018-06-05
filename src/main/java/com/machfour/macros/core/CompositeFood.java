package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Schema;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CompositeFood extends Food {
    private final List<Ingredient> ingredients;

    protected CompositeFood(ColumnData<Food> data, ObjectSource objectSource) {
        super(data, objectSource);
        this.ingredients = new ArrayList<>();
        setFoodType(FoodType.COMPOSITE);
    }

    public List<Ingredient> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public void addIngredient(@NotNull Ingredient i) {
        assert !ingredients.contains(i) && foreignKeyMatches(i, Schema.IngredientTable.COMPOSITE_FOOD_ID, this);
        ingredients.add(i);
    }

    @Override
    public NutritionData getNutritionData() {
        List<NutritionData> nutritionComponents = new ArrayList<>(ingredients.size());
        for (Ingredient i : ingredients) {
            nutritionComponents.add(i.getNutritionData());
        }
        return NutritionData.sum(nutritionComponents);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CompositeFood && super.equals(o);
    }
}