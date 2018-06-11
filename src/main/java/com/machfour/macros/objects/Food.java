package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Food extends MacrosEntity<Food> {

    public static final List<Column<Food, String>> DESCRIPTION_COLUMNS = Arrays.asList(
        Schema.FoodTable.BRAND
        , Schema.FoodTable.VARIETY
        , Schema.FoodTable.NAME
        , Schema.FoodTable.NOTES
        , Schema.FoodTable.INDEX_NAME
    );

    private final List<Serving> servings;
    private Serving defaultServing;

    private NutritionData nutritionData;
    private FoodType foodType;
    private FoodCategory foodCategory;
    private final List<Ingredient> ingredients;

    private Food(ColumnData<Food> dataMap, ObjectSource objectSource) {
        super(dataMap, objectSource);
        servings = new ArrayList<>();
        foodType = null;
        nutritionData = null;
        foodType = FoodType.fromString(dataMap.get(Schema.FoodTable.FOOD_TYPE));
        ingredients = (foodType == FoodType.COMPOSITE) ? new ArrayList<>() : null;
    }

    public List<Ingredient> getIngredients() {
        assert foodType == FoodType.COMPOSITE;
        return Collections.unmodifiableList(ingredients);
    }

    public void addIngredient(@NotNull Ingredient i) {
        assert foodType.equals(FoodType.COMPOSITE)
                && !ingredients.contains(i)
                && MacrosEntity.foreignKeyMatches(i, Schema.IngredientTable.COMPOSITE_FOOD_ID, this);
        ingredients.add(i);
    }

    // quantity corresponds to that contained in the Schema.NutritionDataTable.QUANTITY table
    @NotNull
    public NutritionData getNutritionData() {
        // TODO merge with compositeFood's nutrition data
        if (foodType == FoodType.COMPOSITE) {
            List<NutritionData> nutritionComponents = new ArrayList<>(ingredients.size());
            for (Ingredient i : ingredients) {
                nutritionComponents.add(i.getNutritionData());
            }
            return NutritionData.sum(nutritionComponents);
        } else {
            return nutritionData;
        }
    }

    public NutritionData getNutritionData(double quantity) {
        return getNutritionData().rescale(quantity);
    }

    public void setNutritionData(@NotNull NutritionData nd) {
        assert nutritionData == null && nd != null
            && foreignKeyMatches(nd, Schema.NutritionDataTable.FOOD_ID, this);
        nutritionData = nd;
    }

    public FoodType getFoodType() {
        return foodType;
    }

    public void setFoodCategory(@NotNull FoodCategory c) {
        assert foodCategory == null && foreignKeyMatches(this, Schema.FoodTable.CATEGORY, c);
        foodCategory = c;
    }
    public FoodCategory getFoodCategory() {
        return foodCategory;
    }

    @Override
    public Table<Food> getTable() {
        return table();
    }
    public static Table<Food> table() {
        return Schema.FoodTable.instance();
    }

    @Override
    public Factory<Food> getFactory() {
        return factory();
    }
    public static Factory<Food> factory() {
        return Food::new;
    }

    public void addServing(@NotNull Serving s) {
        assert (!servings.contains(s)) && foreignKeyMatches(s, Schema.ServingTable.FOOD_ID, this);
        servings.add(s);
        if (s.isDefault()) {
            setDefaultServing(s);
        }
    }

    @Nullable
    public Long getUsdaIndex() {
        return getData(Schema.FoodTable.USDA_INDEX);
    }

    @Nullable
    public String getNuttabIndex() {
        return getData(Schema.FoodTable.NUTTAB_INDEX);
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof Food && super.equals(o);
    }

    private String getDescriptionData(Column<Food, String> fieldName) {
        assert (DESCRIPTION_COLUMNS.contains(fieldName));
        return getData(fieldName);
    }

    @NotNull
    public String getShortName() {
        return prettyFormat(false, false, false);
    }

    @NotNull
    public String getLongName() {
        return prettyFormat(true, true, false);
    }

    @NotNull
    public String getMediumName() {
        return prettyFormat(true, false, false);
    }

    @NotNull
    public String getSortableName() {
        return prettyFormat(true, true, true);
    }

    public String getCategoryName() {
        return getData(Schema.FoodTable.CATEGORY);
    }

    private String prettyFormat(boolean withBrand, boolean withNotes, boolean sortable) {
        StringBuilder prettyName = new StringBuilder(getDescriptionData(Schema.FoodTable.NAME));

        String v = getDescriptionData(Schema.FoodTable.VARIETY);

        if (sortable) {
            if (withBrand && hasDescriptionData(Schema.FoodTable.BRAND)) {
                prettyName.append(getDescriptionData(Schema.FoodTable.BRAND)).append(", ");
            }
            if (hasDescriptionData(Schema.FoodTable.VARIETY)) {
                prettyName.append(", ").append(v);
            }
        } else {
            if (hasDescriptionData(Schema.FoodTable.VARIETY)) {
                if (getData(Schema.FoodTable.VARIETY_AFTER_NAME)) {
                    prettyName.append(" ").append(v);
                } else {
                    prettyName.insert(0, v + " ");
                }
            }
            if (withBrand && hasDescriptionData(Schema.FoodTable.BRAND)) {
                prettyName.insert(0, getDescriptionData(Schema.FoodTable.BRAND) + " ");
            }
        }

        if (withNotes && hasDescriptionData(Schema.FoodTable.NOTES)) {
            prettyName.append(" (").append(getDescriptionData(Schema.FoodTable.NOTES)).append(")");
        }

        return prettyName.toString();
    }

    /*
     * Order of fields:
     * if sortable:
     *     <name>, <brand>, <variety> (<notes>)
     * else if variety_after_name is present:
     *     <brand> <name> <variety> (<notes>)
     * else:
     *     <brand> <variety> <name> (<notes>)
     */

    private boolean hasDescriptionData(Column<Food, String> fieldName) {
        return hasData(fieldName);
    }

    private void setDefaultServing(Serving s) {
        assert defaultServing == null : "Default serving already set";
        assert servings.contains(s) : "Serving does not belong to this food";
        this.defaultServing = s;
    }

    @Nullable
    public Serving getDefaultServing() {
        return defaultServing;
    }

    @Nullable
    public Serving getServingById(long servingId) {
        Serving serving = null;
        for (Serving s : servings) {
            if (servingId == s.getId()) {
                serving = s;
            }
        }
        return serving;
    }
    @Nullable
    public Serving getServingByName(@NotNull String name) {
        Serving serving = null;
        for (Serving s : servings) {
            if (name.equals(s.getName())) {
                serving = s;
            }
        }
        return serving;
    }

    public List<Serving> getServings() {
        return Collections.unmodifiableList(servings);
    }
}
