package com.machfour.macros.data;

import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.core.MacrosUtils;
import com.machfour.macros.util.DateStamp;

import java.util.function.Supplier;

import static com.machfour.macros.data.Column.column;

/*
 * List of all columns in the database. All are static objects, and can be comparted via strict object equality.
 */
public class Columns {
    private Columns() {
    }

    public final static class Base {
        public static final Column<Long> ID = column("id", MacrosType.ID, false, false, MacrosPersistable.NO_ID);
        public static final Column<Long> CREATE_TIME = column("create_time", MacrosType.TIMESTAMP, false, false, (Supplier<Long>) MacrosUtils::getCurrentTimeStamp);
        public static final Column<Long> MODIFY_TIME = column("modify_time", MacrosType.TIMESTAMP, false, false, (Supplier<Long>) MacrosUtils::getCurrentTimeStamp);
        public static final Column[] COLUMNS = {ID, CREATE_TIME, MODIFY_TIME};

    }

    public final static class QuantityUnit {
        public static final Column<String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column<String> ABBREVIATION = column("abbreviation", MacrosType.TEXT, true, false);
        public static final Column<Boolean> IS_VOLUME_UNIT = column("is_volume_unit", MacrosType.BOOLEAN, true, false);
        public static final Column<Double> METRIC_EQUIVALENT = column("metric_equivalent", MacrosType.REAL, true, false);
        public static final Column[] COLUMNS = {
            NAME
            , ABBREVIATION
            , IS_VOLUME_UNIT
            , METRIC_EQUIVALENT
        };

        private QuantityUnit() {
        }
    }

    public final static class Food {
        public static final Column<String> INDEX_NAME = column("index_name", MacrosType.TEXT, true, false);
        public static final Column<String> BRAND = column("brand", MacrosType.TEXT, true, false);
        public static final Column<String> COMMERCIAL_NAME = column("commercial_name", MacrosType.TEXT, true, true);
        public static final Column<String> VARIETY_PREFIX_1 = column("variety_prefix_1", MacrosType.TEXT, true, true);
        public static final Column<String> VARIETY_PREFIX_2 = column("variety_prefix_2", MacrosType.TEXT, true, true);
        public static final Column<String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column<String> VARIETY_SUFFIX = column("variety_suffix", MacrosType.TEXT, true, true);
        public static final Column<String> NOTES = column("notes", MacrosType.TEXT, true, true);
        public static final Column<Long> CATEGORY = column("category", MacrosType.ID, false, false);
        public static final Column<String> FOOD_TYPE = column("food_type", MacrosType.TEXT, false, false);
        public static final Column<Double> DENSITY = column("density", MacrosType.REAL, true, true);
        public static final Column<Long> USDA_INDEX = column("usda_index", MacrosType.INTEGER, false, true);
        public static final Column<String> NUTTAB_INDEX = column("nuttab_index", MacrosType.TEXT, false, true);
        public static final Column[] COLUMNS = {
            INDEX_NAME
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
        };

        private Food() {
        }
    }

    public final static class Serving {
        public static final Column<String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column<Double> QUANTITY = column("quantity", MacrosType.REAL, true, false);
        public static final Column<Long> QUANTITY_UNIT = column("quantity_unit", MacrosType.ID, false, false);
        public static final Column<Boolean> IS_DEFAULT = column("is_default", MacrosType.BOOLEAN, true, false);
        public static final Column<Long> FOOD_ID = column("food_id", MacrosType.ID, false, false);
        public static final Column[] COLUMNS = {
            NAME
            , QUANTITY
            , QUANTITY_UNIT
            , IS_DEFAULT
            , FOOD_ID
        };

        private Serving() {
        }
    }

    public final static class FoodPortion {
        public static final Column<Double> QUANTITY = column("quantity", MacrosType.REAL, true, false);
        public static final Column<Long> QUANTITY_UNIT = column("quantity_unit", MacrosType.ID, false, false);
        public static final Column<Long> FOOD_ID = column("food_id", MacrosType.ID, false, false);
        public static final Column<Long> MEAL_ID = column("meal_id", MacrosType.ID, false, false);
        public static final Column<Long> SERVING_ID = column("serving_id", MacrosType.ID, false, true);
        public static final Column<String> NOTES = column("notes", MacrosType.TEXT, true, false);
        public static final Column[] COLUMNS = {
            QUANTITY
            , QUANTITY_UNIT
            , FOOD_ID
            , MEAL_ID
            , SERVING_ID
            , NOTES
        };

        private FoodPortion() {
        }
    }

    public final static class Meal {
        public static final Column<String> DESCRIPTION = column("name", MacrosType.TEXT, true, false);
        public static final Column<DateStamp> DAY = column("name", MacrosType.DATESTAMP, true, false);
        public static final Column[] COLUMNS = {DESCRIPTION, DAY};

        private Meal() {
        }
    }

    public final static class FoodCategory {
        public static final Column<String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column[] COLUMNS = {NAME};

        private FoodCategory() {
        }
    }

    public final static class MealDescription {
        public static final Column<String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column[] COLUMNS = {NAME};

        private MealDescription() {
        }
    }

    public final static class Ingredient {
        public static final Column<Long> COMPOSITE_FOOD_ID = column("composite_food_id", MacrosType.ID, false, false);
        public static final Column<Long> INGREDIENT_FOOD_ID = column("ingredient_food_id", MacrosType.ID, false, false);
        public static final Column<Double> QUANTITY = column("quantity", MacrosType.REAL, false, false);
        public static final Column<Double> PREPARED_QUANTITY = column("prepared_quantity", MacrosType.REAL, false, false);
        public static final Column<Long> QUANTITY_UNIT = column("quantity_unit", MacrosType.ID, false, false);
        public static final Column<Long> SERVING_ID = column("serving_id", MacrosType.ID, false, true);
        public static final Column<String> NOTES = column("notes", MacrosType.TEXT, true, false);
        public static final Column[] COLUMNS = {
            COMPOSITE_FOOD_ID
            , INGREDIENT_FOOD_ID
            , QUANTITY
            , PREPARED_QUANTITY
            , QUANTITY_UNIT
            , SERVING_ID
            , NOTES
        };

        private Ingredient() {
        }
    }

    public final static class RegularMeal {
        public static final Column<String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column<Long> MEAL_ID = column("meal_id", MacrosType.ID, false, false);
        public static final Column[] COLUMNS = {NAME, MEAL_ID};

        private RegularMeal() {
        }
    }

    public final static class NutritionData {
        public static final Column<Long> FOOD_ID = column("food_id", MacrosType.ID, false, true);
        public static final Column<String> DATA_SOURCE = column("data_source", MacrosType.TEXT, true, true);
        public static final Column<Double> QUANTITY = column("quantity", MacrosType.REAL, true, false, 100.0);
        public static final Column<Long> QUANTITY_UNIT = column("quantity_unit", MacrosType.ID, false, false);
        public static final Column<Double> KILOJOULES = column("kilojoules", MacrosType.REAL, true, true);
        public static final Column<Double> CALORIES = column("calories", MacrosType.REAL, true, true);
        public static final Column<Double> PROTEIN = column("protein", MacrosType.REAL, true, true);
        public static final Column<Double> CARBOHYDRATE = column("carbohydrate", MacrosType.REAL, true, true);
        public static final Column<Double> CARBOHYDRATE_BY_DIFF = column("carbohydrate_by_diff", MacrosType.REAL, true, true);
        public static final Column<Double> SUGAR = column("sugar", MacrosType.REAL, true, true);
        public static final Column<Double> SUGAR_ALCOHOL = column("sugar_alcohol", MacrosType.REAL, true, true);
        public static final Column<Double> STARCH = column("starch", MacrosType.REAL, true, true);
        public static final Column<Double> FAT = column("fat", MacrosType.REAL, true, true);
        public static final Column<Double> SATURATED_FAT = column("saturated_fat", MacrosType.REAL, true, true);
        public static final Column<Double> MONOUNSATURATED_FAT = column("monounsaturated_fat", MacrosType.REAL, true, true);
        public static final Column<Double> POLYUNSATURATED_FAT = column("polyunsaturated_fat", MacrosType.REAL, true, true);
        public static final Column<Double> OMEGA_3_FAT = column("omega_3_fat", MacrosType.REAL, true, true);
        public static final Column<Double> OMEGA_6_FAT = column("omega_6_fat", MacrosType.REAL, true, true);
        public static final Column<Double> FIBRE = column("fibre", MacrosType.REAL, true, true);
        public static final Column<Double> SODIUM = column("sodium", MacrosType.REAL, true, true);
        public static final Column<Double> SALT = column("salt", MacrosType.REAL, true, true);
        public static final Column<Double> CALCIUM = column("calcium", MacrosType.REAL, true, true);
        public static final Column<Double> WATER = column("water", MacrosType.REAL, true, true);
        public static final Column<Double> ALCOHOL = column("alcohol", MacrosType.REAL, true, true);
        public static final Column[] COLUMNS = {
            DATA_SOURCE
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
        };


        private NutritionData() {
        }
    }

    public final static class FoodAttribute {
        public static final Column<String> NAME = column("name", MacrosType.TEXT, true, false);
        public static final Column[] COLUMNS = {NAME};

        private FoodAttribute() {
        }
    }

    public final static class AttributeMap {
        public static final Column<Long> FOOD_ID = column("food_id", MacrosType.ID, false, false);
        public static final Column<Long> ATTRIBUTE_ID = column("attribute_id", MacrosType.ID, false, false);
        public static final Column[] COLUMNS = {FOOD_ID, ATTRIBUTE_ID};

        private AttributeMap() {
        }
    }
}
