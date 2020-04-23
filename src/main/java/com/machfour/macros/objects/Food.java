package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.machfour.macros.core.Schema.FoodTable.*;

public class Food extends MacrosEntityImpl<Food> {

    public static final List<Column<Food, String>> DESCRIPTION_COLUMNS = Arrays.asList(
        Schema.FoodTable.BRAND
        , Schema.FoodTable.VARIETY
        , Schema.FoodTable.NAME
        , Schema.FoodTable.NOTES
        , Schema.FoodTable.INDEX_NAME
    );

    private final List<Serving> servings;
    private final String sortableName;
    private Serving defaultServing;

    private NutritionData nutritionData;
    private FoodType foodType;
    private FoodCategory foodCategory;

    protected Food(ColumnData<Food> dataMap, ObjectSource objectSource) {
        super(dataMap, objectSource);
        servings = new ArrayList<>();
        foodType = null;
        nutritionData = null;
        foodType = FoodType.fromString(dataMap.get(Schema.FoodTable.FOOD_TYPE));
        sortableName = makeSortableName();
    }

    // quantity corresponds to that contained in the Schema.NutritionDataTable.QUANTITY table
    @NotNull
    public NutritionData getNutritionData() {
        return nutritionData;
    }

    public void setNutritionData(@NotNull NutritionData nd) {
        assert nutritionData == null && foreignKeyMatches(nd, Schema.NutritionDataTable.FOOD_ID, this);
        nutritionData = nd;
    }

    public FoodType getFoodType() {
        return foodType;
    }

    public void setFoodCategory(@NotNull FoodCategory c) {
        assert foodCategory == null && foreignKeyMatches(this, CATEGORY, c);
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

    // Dynamically create either a Food or CompositeFood depending on the datamap passsed in.
    // Hooray for preferring static constructors over new!!!
    public static Factory<Food> factory() {
        return (dataMap, objectSource) -> {
            if (FoodType.fromString(dataMap.get(Schema.FoodTable.FOOD_TYPE)).equals(FoodType.COMPOSITE)) {
                return new CompositeFood(dataMap, objectSource);
            } else {
                return new Food(dataMap, objectSource);
            }
        };
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
        return getData(USDA_INDEX);
    }

    @Nullable
    public String getNuttabIndex() {
        return getData(NUTTAB_INDEX);
    }

    @Nullable
    public String getNotes() {
        return getData(NOTES);
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
        return prettyFormat(false, false, false, false);
    }

    @NotNull
    public String getLongName() {
        return prettyFormat(true, true, true, false);
    }

    @NotNull
    public String getMediumName() {
        return prettyFormat(true, true, false, false);
    }

    @NotNull
    public String getIndexName() {
        return getData(INDEX_NAME);
    }

    @NotNull
    private String makeSortableName() {
        return prettyFormat(true, true, true, true);
    }
    public String getSortableName() {
        return sortableName;
    }

    public String getCategoryName() {
        return getData(Schema.FoodTable.CATEGORY);
    }

    private String prettyFormat(boolean withBrand, boolean withVariety, boolean withNotes, boolean sortable) {
        StringBuilder prettyName = new StringBuilder(getDescriptionData(Schema.FoodTable.NAME));

        String variety = getDescriptionData(Schema.FoodTable.VARIETY);
        String brand = getDescriptionData(Schema.FoodTable.BRAND);

        if (sortable) {
            if (withBrand && hasDescriptionData(Schema.FoodTable.BRAND)) {
                prettyName.append(", ").append(brand);
            }
            if (hasDescriptionData(Schema.FoodTable.VARIETY)) {
                prettyName.append(", ").append(variety);
            }
        } else {
            if (withVariety && hasDescriptionData(Schema.FoodTable.VARIETY)) {
                if (getData(Schema.FoodTable.VARIETY_AFTER_NAME)) {
                    prettyName.append(" ").append(variety);
                } else {
                    prettyName.insert(0, variety + " ");
                }
            }
            if (withBrand && hasDescriptionData(Schema.FoodTable.BRAND)) {
                prettyName.insert(0, brand + " ");
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
            if (name.equals(s.name())) {
                serving = s;
            }
        }
        return serving;
    }

    public List<Serving> getServings() {
        return Collections.unmodifiableList(servings);
    }
}
