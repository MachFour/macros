package com.machfour.macros.data;

import com.machfour.macros.core.*;
import com.machfour.macros.util.DateStamp;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.machfour.macros.data.Column.column;

/*
 * List of all columns in the database. All are static objects, and can be comparted via strict object equality.
 */
public class Columns {


    private static <M extends MacrosPersistable> Column<M, Long> idColumn(Class<M> MacrosClass) {
        return column("id", MacrosType.ID, false, false, MacrosPersistable.NO_ID);
    }

    private static <M extends MacrosPersistable> Column<M, Long> createTimeColumn(Class<M> MacrosClass) {
        return column("create_time", MacrosType.TIMESTAMP, false, false, (Supplier<Long>) MacrosUtils::getCurrentTimeStamp);
    }

    private static <M extends MacrosPersistable> Column<M, Long> modifyTimeColumn(Class<M> MacrosClass) {
        return column("modify_time", MacrosType.TIMESTAMP, false, false, (Supplier<Long>) MacrosUtils::getCurrentTimeStamp);
    }

    private Columns() {
    }

    public final static class QuantityUnitCol {
        public static final Column<QuantityUnit, Long> ID = idColumn(QuantityUnit.class);
        public static final Column<QuantityUnit, Long> CREATE_TIME = createTimeColumn(QuantityUnit.class);
        public static final Column<QuantityUnit, Long> MODIFY_TIME = modifyTimeColumn(QuantityUnit.class);
        public static final Column<QuantityUnit, String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column<QuantityUnit, String> ABBREVIATION = column("abbreviation", MacrosType.TEXT, true, false);
        public static final Column<QuantityUnit, Boolean> IS_VOLUME_UNIT = column("is_volume_unit", MacrosType.BOOLEAN, true, false);
        public static final Column<QuantityUnit, Double> METRIC_EQUIVALENT = column("metric_equivalent", MacrosType.REAL, true, false);
        public static final List<Column<QuantityUnit, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , NAME
            , ABBREVIATION
            , IS_VOLUME_UNIT
            , METRIC_EQUIVALENT
        );

        private QuantityUnitCol() {
        }
    }

    public final static class FoodCol {
        public static final Column<Food, Long> ID = idColumn(Food.class);
        public static final Column<Food, Long> CREATE_TIME = createTimeColumn(Food.class);
        public static final Column<Food, Long> MODIFY_TIME = modifyTimeColumn(Food.class);
        public static final Column<Food, String> INDEX_NAME = column("index_name", MacrosType.TEXT, true, false);
        public static final Column<Food, String> BRAND = column("brand", MacrosType.TEXT, true, false);
        public static final Column<Food, String> COMMERCIAL_NAME = column("commercial_name", MacrosType.TEXT, true, true);
        public static final Column<Food, String> VARIETY_PREFIX_1 = column("variety_prefix_1", MacrosType.TEXT, true, true);
        public static final Column<Food, String> VARIETY_PREFIX_2 = column("variety_prefix_2", MacrosType.TEXT, true, true);
        public static final Column<Food, String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column<Food, String> VARIETY_SUFFIX = column("variety_suffix", MacrosType.TEXT, true, true);
        public static final Column<Food, String> NOTES = column("notes", MacrosType.TEXT, true, true);
        public static final Column<Food, Long> CATEGORY = column("category", MacrosType.ID, false, false);
        public static final Column<Food, String> FOOD_TYPE = column("food_type", MacrosType.TEXT, false, false);
        public static final Column<Food, Double> DENSITY = column("density", MacrosType.REAL, true, true);
        public static final Column<Food, Long> USDA_INDEX = column("usda_index", MacrosType.INTEGER, false, true);
        public static final Column<Food, String> NUTTAB_INDEX = column("nuttab_index", MacrosType.TEXT, false, true);
        public static final List<Column<Food, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , INDEX_NAME
            , BRAND
            , COMMERCIAL_NAME
            , VARIETY_PREFIX_1
            , VARIETY_PREFIX_2
            , NAME
            , VARIETY_SUFFIX
            , NOTES
            , CATEGORY
            , FOOD_TYPE
            , DENSITY
            , USDA_INDEX
            , NUTTAB_INDEX
        );

        private FoodCol() {
        }
    }

    public final static class ServingCol {
        public static final Column<Serving, Long> ID = idColumn(Serving.class);
        public static final Column<Serving, Long> CREATE_TIME = createTimeColumn(Serving.class);
        public static final Column<Serving, Long> MODIFY_TIME = modifyTimeColumn(Serving.class);
        public static final Column<Serving, String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column<Serving, Double> QUANTITY = column("quantity", MacrosType.REAL, true, false);
        public static final Column<Serving, Long> QUANTITY_UNIT = column("quantity_unit", MacrosType.ID, false, false);
        public static final Column<Serving, Boolean> IS_DEFAULT = column("is_default", MacrosType.BOOLEAN, true, false);
        public static final Column<Serving, Long> FOOD_ID = column("food_id", MacrosType.ID, false, false);
        public static final List<Column<Serving, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , NAME
            , QUANTITY
            , QUANTITY_UNIT
            , IS_DEFAULT
            , FOOD_ID
        );

        private ServingCol() {
        }
    }

    public final static class FoodPortionCol {
        public static final Column<FoodPortion, Long> ID = idColumn(FoodPortion.class);
        public static final Column<FoodPortion, Long> CREATE_TIME = createTimeColumn(FoodPortion.class);
        public static final Column<FoodPortion, Long> MODIFY_TIME = modifyTimeColumn(FoodPortion.class);
        public static final Column<FoodPortion, Double> QUANTITY = column("quantity", MacrosType.REAL, true, false);
        public static final Column<FoodPortion, Long> QUANTITY_UNIT = column("quantity_unit", MacrosType.ID, false, false);
        public static final Column<FoodPortion, Long> FOOD_ID = column("food_id", MacrosType.ID, false, false);
        public static final Column<FoodPortion, Long> MEAL_ID = column("meal_id", MacrosType.ID, false, false);
        public static final Column<FoodPortion, Long> SERVING_ID = column("serving_id", MacrosType.ID, false, true);
        public static final Column<FoodPortion, String> NOTES = column("notes", MacrosType.TEXT, true, false);
        public static final List<Column<FoodPortion, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , QUANTITY
            , QUANTITY_UNIT
            , FOOD_ID
            , MEAL_ID
            , SERVING_ID
            , NOTES
        );

        private FoodPortionCol() {
        }
    }

    public final static class MealCol {
        public static final Column<Meal, Long> ID = idColumn(Meal.class);
        public static final Column<Meal, Long> CREATE_TIME = createTimeColumn(Meal.class);
        public static final Column<Meal, Long> MODIFY_TIME = modifyTimeColumn(Meal.class);
        public static final Column<Meal, String> DESCRIPTION = column("name", MacrosType.TEXT, true, false);
        public static final Column<Meal, DateStamp> DAY = column("name", MacrosType.DATESTAMP, true, false);
        public static final List<Column<Meal, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , DESCRIPTION
            , DAY
        );

        private MealCol() {
        }
    }

    public final static class FoodCategoryCol {
        public static final Column<FoodCategory, Long> ID = idColumn(FoodCategory.class);
        public static final Column<FoodCategory, Long> CREATE_TIME = createTimeColumn(FoodCategory.class);
        public static final Column<FoodCategory, Long> MODIFY_TIME = modifyTimeColumn(FoodCategory.class);
        public static final Column<FoodCategory, String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final List<Column<FoodCategory, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , NAME
        );

        private FoodCategoryCol() {
        }
    }

    public final static class MealDescriptionCol {
        public static final Column<MealDescription, Long> ID = idColumn(MealDescription.class);
        public static final Column<MealDescription, Long> CREATE_TIME = createTimeColumn(MealDescription.class);
        public static final Column<MealDescription, Long> MODIFY_TIME = modifyTimeColumn(MealDescription.class);
        public static final Column<MealDescription, String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final List<Column<MealDescription, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , NAME
        );

        private MealDescriptionCol() {
        }
    }

    public final static class IngredientCol {
        public static final Column<Ingredient, Long> ID = idColumn(Ingredient.class);
        public static final Column<Ingredient, Long> CREATE_TIME = createTimeColumn(Ingredient.class);
        public static final Column<Ingredient, Long> MODIFY_TIME = modifyTimeColumn(Ingredient.class);
        public static final Column<Ingredient, Long> COMPOSITE_FOOD_ID = column("composite_food_id", MacrosType.ID, false, false);
        public static final Column<Ingredient, Long> INGREDIENT_FOOD_ID = column("ingredient_food_id", MacrosType.ID, false, false);
        public static final Column<Ingredient, Double> QUANTITY = column("quantity", MacrosType.REAL, false, false);
        public static final Column<Ingredient, Double> PREPARED_QUANTITY = column("prepared_quantity", MacrosType.REAL, false, false);
        public static final Column<Ingredient, Long> QUANTITY_UNIT = column("quantity_unit", MacrosType.ID, false, false);
        public static final Column<Ingredient, Long> SERVING_ID = column("serving_id", MacrosType.ID, false, true);
        public static final Column<Ingredient, String> NOTES = column("notes", MacrosType.TEXT, true, false);
        public static final List<Column<Ingredient, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , COMPOSITE_FOOD_ID
            , INGREDIENT_FOOD_ID
            , QUANTITY
            , PREPARED_QUANTITY
            , QUANTITY_UNIT
            , SERVING_ID
            , NOTES
        );

        private IngredientCol() {
        }
    }

    public final static class RegularMealCol {
        public static final Column<RegularMeal, Long> ID = idColumn(RegularMeal.class);
        public static final Column<RegularMeal, Long> CREATE_TIME = createTimeColumn(RegularMeal.class);
        public static final Column<RegularMeal, Long> MODIFY_TIME = modifyTimeColumn(RegularMeal.class);
        public static final Column<RegularMeal, String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column<RegularMeal, Long> MEAL_ID = column("meal_id", MacrosType.ID, false, false);
        public static final List<Column<RegularMeal, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , NAME
            , MEAL_ID
        );

        private RegularMealCol() {
        }
    }

    public final static class NutritionDataCol {
        public static final Column<NutritionData, Long> ID = idColumn(NutritionData.class);
        public static final Column<NutritionData, Long> CREATE_TIME = createTimeColumn(NutritionData.class);
        public static final Column<NutritionData, Long> MODIFY_TIME = modifyTimeColumn(NutritionData.class);
        public static final Column<NutritionData, Long> FOOD_ID = column("food_id", MacrosType.ID, false, true);
        public static final Column<NutritionData, String> DATA_SOURCE = column("data_source", MacrosType.TEXT, true, true);
        public static final Column<NutritionData, Double> QUANTITY = column("quantity", MacrosType.REAL, true, false, 100.0);
        public static final Column<NutritionData, Long> QUANTITY_UNIT = column("quantity_unit", MacrosType.ID, false, false);
        public static final Column<NutritionData, Double> KILOJOULES = column("kilojoules", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> CALORIES = column("calories", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> PROTEIN = column("protein", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> CARBOHYDRATE = column("carbohydrate", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> CARBOHYDRATE_BY_DIFF = column("carbohydrate_by_diff", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> SUGAR = column("sugar", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> SUGAR_ALCOHOL = column("sugar_alcohol", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> STARCH = column("starch", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> FAT = column("fat", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> SATURATED_FAT = column("saturated_fat", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> MONOUNSATURATED_FAT = column("monounsaturated_fat", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> POLYUNSATURATED_FAT = column("polyunsaturated_fat", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> OMEGA_3_FAT = column("omega_3_fat", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> OMEGA_6_FAT = column("omega_6_fat", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> FIBRE = column("fibre", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> SODIUM = column("sodium", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> SALT = column("salt", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> CALCIUM = column("calcium", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> WATER = column("water", MacrosType.REAL, true, true);
        public static final Column<NutritionData, Double> ALCOHOL = column("alcohol", MacrosType.REAL, true, true);
        public static final List<Column<NutritionData, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , DATA_SOURCE
            , QUANTITY
            , QUANTITY_UNIT
            , KILOJOULES
            , CALORIES
            , PROTEIN
            , CARBOHYDRATE
            , CARBOHYDRATE_BY_DIFF
            , SUGAR
            , SUGAR_ALCOHOL
            , STARCH
            , FAT
            , SATURATED_FAT
            , MONOUNSATURATED_FAT
            , POLYUNSATURATED_FAT
            , OMEGA_3_FAT
            , OMEGA_6_FAT
            , FIBRE
            , SODIUM
            , SALT
            , CALCIUM
            , WATER
            , ALCOHOL
        );


        private NutritionDataCol() {
        }
    }

    public final static class FoodAttributeCol {
        public static final Column<FoodAttribute, Long> ID = idColumn(FoodAttribute.class);
        public static final Column<FoodAttribute, Long> CREATE_TIME = createTimeColumn(FoodAttribute.class);
        public static final Column<FoodAttribute, Long> MODIFY_TIME = modifyTimeColumn(FoodAttribute.class);
        public static final Column<FoodAttribute, String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final List<Column<FoodAttribute, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , NAME
        );

        private FoodAttributeCol() {
        }
    }

    public final static class AttributeMapCol {
        public static final Column<AttributeMap, Long> ID = idColumn(AttributeMap.class);
        public static final Column<AttributeMap, Long> CREATE_TIME = createTimeColumn(AttributeMap.class);
        public static final Column<AttributeMap, Long> MODIFY_TIME = modifyTimeColumn(AttributeMap.class);
        public static final Column<AttributeMap, Long> FOOD_ID = column("food_id", MacrosType.ID, false, false);
        public static final Column<AttributeMap, Long> ATTRIBUTE_ID = column("attribute_id", MacrosType.ID, false, false);
        public static final List<Column<AttributeMap, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , FOOD_ID
            , ATTRIBUTE_ID
        );

        private AttributeMapCol() {
        }
    }
}
