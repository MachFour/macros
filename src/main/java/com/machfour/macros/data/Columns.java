package com.machfour.macros.data;

import com.machfour.macros.core.*;
import com.machfour.macros.util.DateStamp;
import com.machfour.macros.util.Supplier;

import java.util.Arrays;
import java.util.List;

import static com.machfour.macros.data.Column.column;
import static com.machfour.macros.data.Types.*;

/*
 * List of all columns in the database. All are static objects, and can be comparted via strict object equality.
 */
public class Columns {


    private static <M> Column<M, Id, Long> idColumn(int index, Class<M> MacrosClass) {
        return column(index, "id", ID, false, false, MacrosPersistable.NO_ID);
    }

    private static <M> Column<M, Time, Long> createTimeColumn(int index, Class<M> MacrosClass) {
        return column(index, "create_time", TIMESTAMP, false, false, (Supplier<Long>) MacrosUtils::getCurrentTimeStamp);
    }

    private static <M> Column<M, Time, Long> modifyTimeColumn(int index, Class<M> MacrosClass) {
        return column(index, "modify_time", TIMESTAMP, false, false, (Supplier<Long>) MacrosUtils::getCurrentTimeStamp);
    }

    private Columns() {
    }

    public final static class QuantityUnitCol {
        public static final Column<QuantityUnit, Id, Long> ID = idColumn(0, QuantityUnit.class);
        public static final Column<QuantityUnit, Time, Long> CREATE_TIME = createTimeColumn(1, QuantityUnit.class);
        public static final Column<QuantityUnit, Time, Long> MODIFY_TIME = modifyTimeColumn(2, QuantityUnit.class);
        public static final Column<QuantityUnit, Text, String> NAME = column(3, "name", Types.TEXT, true, false);
        public static final Column<QuantityUnit, Text, String> ABBREVIATION = column(4, "abbreviation", Types.TEXT, true, false);
        public static final Column<QuantityUnit, Bool, Boolean> IS_VOLUME_UNIT = column(5, "is_volume_unit", Types.BOOLEAN, true, false);
        public static final Column<QuantityUnit, Real, Double> METRIC_EQUIVALENT = column(6, "metric_equivalent", Types.REAL, true, false);
        public static final List<Column<QuantityUnit, ?, ?>> COLUMNS = Arrays.asList(
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
        public static final Column<Food, Id, Long> ID = idColumn(0, Food.class);
        public static final Column<Food, Time, Long> CREATE_TIME = createTimeColumn(1, Food.class);
        public static final Column<Food, Time, Long> MODIFY_TIME = modifyTimeColumn(2, Food.class);
        public static final Column<Food, Text, String> INDEX_NAME = column(3, "index_name", Types.TEXT, true, false);
        public static final Column<Food, Text, String> BRAND = column(4, "brand", Types.TEXT, true, false);
        public static final Column<Food, Text, String> COMMERCIAL_NAME = column(5, "commercial_name", Types.TEXT, true, true);
        public static final Column<Food, Text, String> VARIETY_PREFIX_1 = column(6, "variety_prefix_1", Types.TEXT, true, true);
        public static final Column<Food, Text, String> VARIETY_PREFIX_2 = column(7, "variety_prefix_2", Types.TEXT, true, true);
        public static final Column<Food, Text, String> NAME = column(8, "name", Types.TEXT, true, false);
        public static final Column<Food, Text, String> VARIETY_SUFFIX = column(9, "variety_suffix", Types.TEXT, true, true);
        public static final Column<Food, Text, String> NOTES = column(10, "notes", Types.TEXT, true, true);
        public static final Column<Food, Id, Long> CATEGORY = column(11, "category", Types.ID, false, false);
        public static final Column<Food, Text, String> FOOD_TYPE = column(12, "food_type", Types.TEXT, false, false);
        public static final Column<Food, Real, Double> DENSITY = column(13, "density", Types.REAL, true, true);
        public static final Column<Food, Int, Long> USDA_INDEX = column(14, "usda_index", Types.INTEGER, false, true);
        public static final Column<Food, Text, String> NUTTAB_INDEX = column(15, "nuttab_index", Types.TEXT, false, true);
        public static final List<Column<Food, ?, ?>> COLUMNS = Arrays.asList(
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
        public static final Column<Serving, Id, Long> ID = idColumn(0, Serving.class);
        public static final Column<Serving, Time, Long> CREATE_TIME = createTimeColumn(1, Serving.class);
        public static final Column<Serving, Time, Long> MODIFY_TIME = modifyTimeColumn(2, Serving.class);
        public static final Column<Serving, Text, String> NAME = column(3, "name", Types.TEXT, true, false);
        public static final Column<Serving, Real, Double> QUANTITY = column(4, "quantity", Types.REAL, true, false);
        public static final Column<Serving, Id, Long> QUANTITY_UNIT = column(5, "quantity_unit", Types.ID, false, false);
        public static final Column<Serving, Bool, Boolean> IS_DEFAULT = column(6, "is_default", Types.BOOLEAN, true, false);
        public static final Column<Serving, Id, Long> FOOD_ID = column(7, "food_id", Types.ID, false, false);
        public static final List<Column<Serving, ?, ?>> COLUMNS = Arrays.asList(
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
        public static final Column<FoodPortion, Id, Long> ID = idColumn(0, FoodPortion.class);
        public static final Column<FoodPortion, Time, Long> CREATE_TIME = createTimeColumn(1, FoodPortion.class);
        public static final Column<FoodPortion, Time, Long> MODIFY_TIME = modifyTimeColumn(2, FoodPortion.class);
        public static final Column<FoodPortion, Real, Double> QUANTITY = column(3, "quantity", Types.REAL, true, false);
        public static final Column<FoodPortion, Id, Long> QUANTITY_UNIT = column(4, "quantity_unit", Types.ID, false, false);
        public static final Column<FoodPortion, Id, Long> FOOD_ID = column(5, "food_id", Types.ID, false, false);
        public static final Column<FoodPortion, Id, Long> MEAL_ID = column(6, "meal_id", Types.ID, false, false);
        public static final Column<FoodPortion, Id, Long> SERVING_ID = column(7, "serving_id", Types.ID, false, true);
        public static final Column<FoodPortion, Text, String> NOTES = column(8, "notes", Types.TEXT, true, false);
        public static final List<Column<FoodPortion, ?, ?>> COLUMNS = Arrays.asList(
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
        public static final Column<Meal, Id, Long> ID = idColumn(0, Meal.class);
        public static final Column<Meal, Time, Long> CREATE_TIME = createTimeColumn(1, Meal.class);
        public static final Column<Meal, Time, Long> MODIFY_TIME = modifyTimeColumn(2, Meal.class);
        public static final Column<Meal, Text, String> DESCRIPTION = column(3, "name", Types.TEXT, true, false);
        public static final Column<Meal, Date, DateStamp> DAY = column(4, "name", Types.DATESTAMP, true, false);
        public static final List<Column<Meal, ?, ?>> COLUMNS = Arrays.asList(
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
        public static final Column<FoodCategory, Id, Long> ID = idColumn(0, FoodCategory.class);
        public static final Column<FoodCategory, Time, Long> CREATE_TIME = createTimeColumn(1, FoodCategory.class);
        public static final Column<FoodCategory, Time, Long> MODIFY_TIME = modifyTimeColumn(2, FoodCategory.class);
        public static final Column<FoodCategory, Text, String> NAME = column(3, "name", Types.TEXT, true, false);
        public static final List<Column<FoodCategory, ?, ?>> COLUMNS = Arrays.asList(
              ID
            , CREATE_TIME
            , MODIFY_TIME
            , NAME
        );

        private FoodCategoryCol() {
        }
    }

    public final static class MealDescriptionCol {
        public static final Column<MealDescription, Id, Long> ID = idColumn(0, MealDescription.class);
        public static final Column<MealDescription, Time, Long> CREATE_TIME = createTimeColumn(1, MealDescription.class);
        public static final Column<MealDescription, Time, Long> MODIFY_TIME = modifyTimeColumn(2, MealDescription.class);
        public static final Column<MealDescription, Text, String> NAME = column(3, "name", Types.TEXT, true, false);
        public static final List<Column<MealDescription, ?, ?>> COLUMNS = Arrays.asList(
              ID
            , CREATE_TIME
            , MODIFY_TIME
            , NAME
        );

        private MealDescriptionCol() {
        }
    }

    public final static class IngredientCol {
        public static final Column<Ingredient, Id, Long> ID = idColumn(0, Ingredient.class);
        public static final Column<Ingredient, Time, Long> CREATE_TIME = createTimeColumn(1, Ingredient.class);
        public static final Column<Ingredient, Time, Long> MODIFY_TIME = modifyTimeColumn(2, Ingredient.class);
        public static final Column<Ingredient, Id, Long> COMPOSITE_FOOD_ID = column(3, "composite_food_id", Types.ID, false, false);
        public static final Column<Ingredient, Id, Long> INGREDIENT_FOOD_ID = column(4, "ingredient_food_id", Types.ID, false, false);
        public static final Column<Ingredient, Real, Double> QUANTITY = column(5, "quantity", Types.REAL, false, false);
        public static final Column<Ingredient, Real, Double> PREPARED_QUANTITY = column(6, "prepared_quantity", Types.REAL, false, false);
        public static final Column<Ingredient, Id, Long> QUANTITY_UNIT = column(7, "quantity_unit", Types.ID, false, false);
        public static final Column<Ingredient, Id, Long> SERVING_ID = column(8, "serving_id", Types.ID, false, true);
        public static final Column<Ingredient, Text, String> NOTES = column(9, "notes", Types.TEXT, true, false);
        public static final List<Column<Ingredient, ?, ?>> COLUMNS = Arrays.asList(
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
        public static final Column<RegularMeal, Id, Long> ID = idColumn(0, RegularMeal.class);
        public static final Column<RegularMeal, Time, Long> CREATE_TIME = createTimeColumn(1, RegularMeal.class);
        public static final Column<RegularMeal, Time, Long> MODIFY_TIME = modifyTimeColumn(2, RegularMeal.class);
        public static final Column<RegularMeal, Text, String> NAME = column(3, "name", Types.TEXT, true, false);
        public static final Column<RegularMeal, Id, Long> MEAL_ID = column(4, "meal_id", Types.ID, false, false);
        public static final List<Column<RegularMeal, ?, ?>> COLUMNS = Arrays.asList(
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
        public static final Column<NutritionData, Id, Long> ID = idColumn(0, NutritionData.class);
        public static final Column<NutritionData, Time, Long> CREATE_TIME = createTimeColumn(1, NutritionData.class);
        public static final Column<NutritionData, Time, Long> MODIFY_TIME = modifyTimeColumn(2, NutritionData.class);
        public static final Column<NutritionData, Id, Long> FOOD_ID = column(3, "food_id", Types.ID, false, true);
        public static final Column<NutritionData, Text, String> DATA_SOURCE = column(4, "data_source", Types.TEXT, true, true);
        public static final Column<NutritionData, Real, Double> QUANTITY = column(5, "quantity", Types.REAL, true, false, 100.0);
        public static final Column<NutritionData, Id, Long> QUANTITY_UNIT = column(6, "quantity_unit", Types.ID, false, false);
        public static final Column<NutritionData, Real, Double> KILOJOULES = column(7, "kilojoules", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> CALORIES = column(8, "calories", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> PROTEIN = column(9, "protein", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> CARBOHYDRATE = column(10, "carbohydrate", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> CARBOHYDRATE_BY_DIFF = column(11, "carbohydrate_by_diff", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SUGAR = column(12, "sugar", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SUGAR_ALCOHOL = column(13, "sugar_alcohol", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> STARCH = column(14, "starch", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> FAT = column(15, "fat", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SATURATED_FAT = column(16, "saturated_fat", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> MONOUNSATURATED_FAT = column(17, "monounsaturated_fat", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> POLYUNSATURATED_FAT = column(18, "polyunsaturated_fat", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> OMEGA_3_FAT = column(19, "omega_3", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> OMEGA_6_FAT = column(20, "omega_6", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> FIBRE = column(21, "fibre", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SODIUM = column(22, "sodium", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SALT = column(23, "salt", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> CALCIUM = column(24, "calcium", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> WATER = column(25, "water", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> ALCOHOL = column(26, "alcohol", Types.REAL, true, true);
        public static final List<Column<NutritionData, ?, ?>> COLUMNS = Arrays.asList(
              ID
            , CREATE_TIME
            , MODIFY_TIME
            , FOOD_ID
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
        public static final Column<FoodAttribute, Id, Long> ID = idColumn(0, FoodAttribute.class);
        public static final Column<FoodAttribute, Time, Long> CREATE_TIME = createTimeColumn(1, FoodAttribute.class);
        public static final Column<FoodAttribute, Time, Long> MODIFY_TIME = modifyTimeColumn(2, FoodAttribute.class);
        public static final Column<FoodAttribute, Text, String> NAME = column(3, "name", Types.TEXT, true, false);
        public static final List<Column<FoodAttribute, ?, ?>> COLUMNS = Arrays.asList(
                ID
            , CREATE_TIME
            , MODIFY_TIME
            , NAME
        );

        private FoodAttributeCol() {
        }
    }

    public final static class AttributeMapCol {
        public static final Column<AttributeMap, Id, Long> ID = idColumn(0, AttributeMap.class);
        public static final Column<AttributeMap, Time, Long> CREATE_TIME = createTimeColumn(1, AttributeMap.class);
        public static final Column<AttributeMap, Time, Long> MODIFY_TIME = modifyTimeColumn(2, AttributeMap.class);
        public static final Column<AttributeMap, Id, Long> FOOD_ID = column(3, "food_id", Types.ID, false, false);
        public static final Column<AttributeMap, Id, Long> ATTRIBUTE_ID = column(3, "attribute_id", Types.ID, false, false);
        public static final List<Column<AttributeMap, ?, ?>> COLUMNS = Arrays.asList(
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
