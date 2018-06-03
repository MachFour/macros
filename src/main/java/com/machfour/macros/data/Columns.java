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


    private static <M> Column<M, Id, Long> idColumn(Class<M> MacrosClass) {
        return column("id", ID, false, false, MacrosPersistable.NO_ID);
    }

    private static <M> Column<M, Time, Long> createTimeColumn(Class<M> MacrosClass) {
        return column("create_time", TIMESTAMP, false, false, (Supplier<Long>) MacrosUtils::getCurrentTimeStamp);
    }

    private static <M> Column<M, Time, Long> modifyTimeColumn(Class<M> MacrosClass) {
        return column("modify_time", TIMESTAMP, false, false, (Supplier<Long>) MacrosUtils::getCurrentTimeStamp);
    }

    private Columns() {
    }

    public final static class QuantityUnitCol {
        public static final Column<QuantityUnit, Id, Long> ID = idColumn(QuantityUnit.class);
        public static final Column<QuantityUnit, Time, Long> CREATE_TIME = createTimeColumn(QuantityUnit.class);
        public static final Column<QuantityUnit, Time, Long> MODIFY_TIME = modifyTimeColumn(QuantityUnit.class);
        public static final Column<QuantityUnit, Text, String> NAME = column("name", Types.TEXT, true, false);
        public static final Column<QuantityUnit, Text, String> ABBREVIATION = column("abbreviation", Types.TEXT, true, false);
        public static final Column<QuantityUnit, Bool, Boolean> IS_VOLUME_UNIT = column("is_volume_unit", Types.BOOLEAN, true, false);
        public static final Column<QuantityUnit, Real, Double> METRIC_EQUIVALENT = column("metric_equivalent", Types.REAL, true, false);
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
        public static final Column<Food, Id, Long> ID = idColumn(Food.class);
        public static final Column<Food, Time, Long> CREATE_TIME = createTimeColumn(Food.class);
        public static final Column<Food, Time, Long> MODIFY_TIME = modifyTimeColumn(Food.class);
        public static final Column<Food, Text, String> INDEX_NAME = column("index_name", Types.TEXT, true, false);
        public static final Column<Food, Text, String> BRAND = column("brand", Types.TEXT, true, false);
        public static final Column<Food, Text, String> COMMERCIAL_NAME = column("commercial_name", Types.TEXT, true, true);
        public static final Column<Food, Text, String> VARIETY_PREFIX_1 = column("variety_prefix_1", Types.TEXT, true, true);
        public static final Column<Food, Text, String> VARIETY_PREFIX_2 = column("variety_prefix_2", Types.TEXT, true, true);
        public static final Column<Food, Text, String> NAME = column("name", Types.TEXT, true, false);
        public static final Column<Food, Text, String> VARIETY_SUFFIX = column("variety_suffix", Types.TEXT, true, true);
        public static final Column<Food, Text, String> NOTES = column("notes", Types.TEXT, true, true);
        public static final Column<Food, Id, Long> CATEGORY = column("category", Types.ID, false, false);
        public static final Column<Food, Text, String> FOOD_TYPE = column("food_type", Types.TEXT, false, false);
        public static final Column<Food, Real, Double> DENSITY = column("density", Types.REAL, true, true);
        public static final Column<Food, Int, Long> USDA_INDEX = column("usda_index", Types.INTEGER, false, true);
        public static final Column<Food, Text, String> NUTTAB_INDEX = column("nuttab_index", Types.TEXT, false, true);
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
        public static final Column<Serving, Id, Long> ID = idColumn(Serving.class);
        public static final Column<Serving, Time, Long> CREATE_TIME = createTimeColumn(Serving.class);
        public static final Column<Serving, Time, Long> MODIFY_TIME = modifyTimeColumn(Serving.class);
        public static final Column<Serving, Text, String> NAME = column("name", Types.TEXT, true, false);
        public static final Column<Serving, Real, Double> QUANTITY = column("quantity", Types.REAL, true, false);
        public static final Column<Serving, Id, Long> QUANTITY_UNIT = column("quantity_unit", Types.ID, false, false);
        public static final Column<Serving, Bool, Boolean> IS_DEFAULT = column("is_default", Types.BOOLEAN, true, false);
        public static final Column<Serving, Id, Long> FOOD_ID = column("food_id", Types.ID, false, false);
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
        public static final Column<FoodPortion, Id, Long> ID = idColumn(FoodPortion.class);
        public static final Column<FoodPortion, Time, Long> CREATE_TIME = createTimeColumn(FoodPortion.class);
        public static final Column<FoodPortion, Time, Long> MODIFY_TIME = modifyTimeColumn(FoodPortion.class);
        public static final Column<FoodPortion, Real, Double> QUANTITY = column("quantity", Types.REAL, true, false);
        public static final Column<FoodPortion, Id, Long> QUANTITY_UNIT = column("quantity_unit", Types.ID, false, false);
        public static final Column<FoodPortion, Id, Long> FOOD_ID = column("food_id", Types.ID, false, false);
        public static final Column<FoodPortion, Id, Long> MEAL_ID = column("meal_id", Types.ID, false, false);
        public static final Column<FoodPortion, Id, Long> SERVING_ID = column("serving_id", Types.ID, false, true);
        public static final Column<FoodPortion, Text, String> NOTES = column("notes", Types.TEXT, true, false);
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
        public static final Column<Meal, Id, Long> ID = idColumn(Meal.class);
        public static final Column<Meal, Time, Long> CREATE_TIME = createTimeColumn(Meal.class);
        public static final Column<Meal, Time, Long> MODIFY_TIME = modifyTimeColumn(Meal.class);
        public static final Column<Meal, Text, String> DESCRIPTION = column("name", Types.TEXT, true, false);
        public static final Column<Meal, Date, DateStamp> DAY = column("name", Types.DATESTAMP, true, false);
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
        public static final Column<FoodCategory, Id, Long> ID = idColumn(FoodCategory.class);
        public static final Column<FoodCategory, Time, Long> CREATE_TIME = createTimeColumn(FoodCategory.class);
        public static final Column<FoodCategory, Time, Long> MODIFY_TIME = modifyTimeColumn(FoodCategory.class);
        public static final Column<FoodCategory, Text, String> NAME = column("name", Types.TEXT, true, false);
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
        public static final Column<MealDescription, Id, Long> ID = idColumn(MealDescription.class);
        public static final Column<MealDescription, Time, Long> CREATE_TIME = createTimeColumn(MealDescription.class);
        public static final Column<MealDescription, Time, Long> MODIFY_TIME = modifyTimeColumn(MealDescription.class);
        public static final Column<MealDescription, Text, String> NAME = column("name", Types.TEXT, true, false);
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
        public static final Column<Ingredient, Id, Long> ID = idColumn(Ingredient.class);
        public static final Column<Ingredient, Time, Long> CREATE_TIME = createTimeColumn(Ingredient.class);
        public static final Column<Ingredient, Time, Long> MODIFY_TIME = modifyTimeColumn(Ingredient.class);
        public static final Column<Ingredient, Id, Long> COMPOSITE_FOOD_ID = column("composite_food_id", Types.ID, false, false);
        public static final Column<Ingredient, Id, Long> INGREDIENT_FOOD_ID = column("ingredient_food_id", Types.ID, false, false);
        public static final Column<Ingredient, Real, Double> QUANTITY = column("quantity", Types.REAL, false, false);
        public static final Column<Ingredient, Real, Double> PREPARED_QUANTITY = column("prepared_quantity", Types.REAL, false, false);
        public static final Column<Ingredient, Id, Long> QUANTITY_UNIT = column("quantity_unit", Types.ID, false, false);
        public static final Column<Ingredient, Id, Long> SERVING_ID = column("serving_id", Types.ID, false, true);
        public static final Column<Ingredient, Text, String> NOTES = column("notes", Types.TEXT, true, false);
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
        public static final Column<RegularMeal, Id, Long> ID = idColumn(RegularMeal.class);
        public static final Column<RegularMeal, Time, Long> CREATE_TIME = createTimeColumn(RegularMeal.class);
        public static final Column<RegularMeal, Time, Long> MODIFY_TIME = modifyTimeColumn(RegularMeal.class);
        public static final Column<RegularMeal, Text, String> NAME = column("name", Types.TEXT, true, false);
        public static final Column<RegularMeal, Id, Long> MEAL_ID = column("meal_id", Types.ID, false, false);
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
        public static final Column<NutritionData, Id, Long> ID = idColumn(NutritionData.class);
        public static final Column<NutritionData, Time, Long> CREATE_TIME = createTimeColumn(NutritionData.class);
        public static final Column<NutritionData, Time, Long> MODIFY_TIME = modifyTimeColumn(NutritionData.class);
        public static final Column<NutritionData, Id, Long> FOOD_ID = column("food_id", Types.ID, false, true);
        public static final Column<NutritionData, Text, String> DATA_SOURCE = column("data_source", Types.TEXT, true, true);
        public static final Column<NutritionData, Real, Double> QUANTITY = column("quantity", Types.REAL, true, false, 100.0);
        public static final Column<NutritionData, Id, Long> QUANTITY_UNIT = column("quantity_unit", Types.ID, false, false);
        public static final Column<NutritionData, Real, Double> KILOJOULES = column("kilojoules", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> CALORIES = column("calories", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> PROTEIN = column("protein", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> CARBOHYDRATE = column("carbohydrate", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> CARBOHYDRATE_BY_DIFF = column("carbohydrate_by_diff", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SUGAR = column("sugar", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SUGAR_ALCOHOL = column("sugar_alcohol", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> STARCH = column("starch", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> FAT = column("fat", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SATURATED_FAT = column("saturated_fat", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> MONOUNSATURATED_FAT = column("monounsaturated_fat", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> POLYUNSATURATED_FAT = column("polyunsaturated_fat", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> OMEGA_3_FAT = column("omega_3", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> OMEGA_6_FAT = column("omega_6", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> FIBRE = column("fibre", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SODIUM = column("sodium", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> SALT = column("salt", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> CALCIUM = column("calcium", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> WATER = column("water", Types.REAL, true, true);
        public static final Column<NutritionData, Real, Double> ALCOHOL = column("alcohol", Types.REAL, true, true);
        public static final List<Column<NutritionData, ?, ?>> COLUMNS = Arrays.asList(
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
        public static final Column<FoodAttribute, Id, Long> ID = idColumn(FoodAttribute.class);
        public static final Column<FoodAttribute, Time, Long> CREATE_TIME = createTimeColumn(FoodAttribute.class);
        public static final Column<FoodAttribute, Time, Long> MODIFY_TIME = modifyTimeColumn(FoodAttribute.class);
        public static final Column<FoodAttribute, Text, String> NAME = column("name", Types.TEXT, true, false);
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
        public static final Column<AttributeMap, Id, Long> ID = idColumn(AttributeMap.class);
        public static final Column<AttributeMap, Time, Long> CREATE_TIME = createTimeColumn(AttributeMap.class);
        public static final Column<AttributeMap, Time, Long> MODIFY_TIME = modifyTimeColumn(AttributeMap.class);
        public static final Column<AttributeMap, Id, Long> FOOD_ID = column("food_id", Types.ID, false, false);
        public static final Column<AttributeMap, Id, Long> ATTRIBUTE_ID = column("attribute_id", Types.ID, false, false);
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
