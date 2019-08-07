package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeFood extends Food {

    // cached sum of ingredients' nutrition data, combined with any overriding data belonging to this food
    private NutritionData ingredientNutritionData;
    private boolean hasOverridingNutritionData;
    private final List<Ingredient> ingredients;

    protected CompositeFood(ColumnData<Food> dataMap, ObjectSource objectSource) {
        super(dataMap, objectSource);
        assert (FoodType.fromString(dataMap.get(Schema.FoodTable.FOOD_TYPE)).equals(FoodType.COMPOSITE));

        ingredients = new ArrayList<>();
        ingredientNutritionData = null;
        hasOverridingNutritionData = false;
    }
    // uses data from the ingredients to add to the existing nutrition data
    private NutritionData calculateIngredientsNutritionData() {
        List<NutritionData> nutritionComponents = new ArrayList<>(ingredients.size());
        for (Ingredient i : ingredients) {
            nutritionComponents.add(i.getNutritionData());
        }
        return NutritionData.sum(nutritionComponents);
    }

    // Sets this Composite food's (overriding) nutrition data
    // TODO call this
    @Override
    public void setNutritionData(@NotNull NutritionData nd) {
        super.setNutritionData(nd);
        hasOverridingNutritionData = true;
    }

    @Override
    @NotNull
    public NutritionData getNutritionData() {
        if (ingredientNutritionData == null) {
            ingredientNutritionData = calculateIngredientsNutritionData();
        }

        if (hasOverridingNutritionData) {
            // combine missing data from the foods nutrirtionData with
            NutritionData overridingData = super.getNutritionData();
            return NutritionData.combine(overridingData, ingredientNutritionData);
        } else {
            return ingredientNutritionData;
        }
    }

    public List<Ingredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    public void addIngredient(@NotNull Ingredient i) {
        assert !ingredients.contains(i)
                && MacrosEntity.foreignKeyMatches(i, Schema.IngredientTable.COMPOSITE_FOOD_ID, this);
        ingredients.add(i);
    }

}
