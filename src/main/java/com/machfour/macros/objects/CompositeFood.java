package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        // don't combine densities of the foods
        return NutritionData.sum(nutritionComponents, false);
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

    /*
     * TODO save the result of this into the database
     *
     */
    /*
      NOTE: combined density is estimated using a weighted sum of the densities of the components.
      This is obviously inaccurate if any item does not have the density recorded,
      HOWEVER ALSO, density of foods will more often than not change during preparation
      (e.g dry ingredients absorbing moisture).
      So really, it probably doesn't make sense to propagate the combined ingredients density value
     */
    public NutritionData getNutritionData() {
        if (ingredientNutritionData == null) {
            ingredientNutritionData = calculateIngredientsNutritionData();
        }

        if (hasOverridingNutritionData) {
            // combine missing data from the foods nData with the overriding data
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
        // sort by ID ~> attempt to keep same order as entered by user or imported
        // note - this is essentially an insertion sort, pretty slow, but most foods shouldn't have too many ingredients
        // TODO API level on android for list.sort()
        Collections.sort(ingredients, (i1, i2) -> Long.compare(i1.getId(), i2.getId()));
    }

}
