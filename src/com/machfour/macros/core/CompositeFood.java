package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;

import java.util.ArrayList;
import java.util.List;

public class CompositeFood extends Food {
    private List<Ingredient> ingredients;

    protected CompositeFood(ColumnData<Food> data, boolean isFromDb) {
        super(data, isFromDb);
        this.ingredients = new ArrayList<>();
        setFoodType(FoodType.COMPOSITE);
    }

    public List<Ingredient> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public void addIngredient(Ingredient i) {
        assert (getId().equals(i.getCompositeFoodId()));
        assert (equals(i.getCompositeFood()));
        assert (!ingredients.contains(i));
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