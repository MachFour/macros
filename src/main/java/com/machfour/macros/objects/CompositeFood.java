package com.machfour.macros.objects;

import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Schema;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeFood extends Food {

    private NutritionData ingredientNutritionData;
    private final List<Ingredient> ingredients;

    protected CompositeFood(ColumnData<Food> dataMap, ObjectSource objectSource) {
        super(dataMap, objectSource);
        assert (FoodType.fromString(dataMap.get(Schema.FoodTable.FOOD_TYPE)).equals(FoodType.COMPOSITE));

        ingredients = new ArrayList<>();
        ingredientNutritionData = null;
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
    @Override
    public void setNutritionData(@NotNull NutritionData nd) {
        super.setNutritionData(nd);
    }

    @Override
    @NotNull
    public NutritionData getNutritionData() {
        if (ingredientNutritionData == null) {
            ingredientNutritionData = calculateIngredientsNutritionData();
        }
        NutritionData overridingData = super.getNutritionData();
        // combine missing data from the foods nutrirtionData with
        return NutritionData.combine(overridingData, ingredientNutritionData);
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
