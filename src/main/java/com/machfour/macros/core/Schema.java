package com.machfour.macros.core;

import com.machfour.macros.core.datatype.MacrosType;
import com.machfour.macros.core.datatype.Types;
import com.machfour.macros.objects.*;
import com.machfour.macros.util.DateStamp;

import java.util.Arrays;
import java.util.Collections;

import static com.machfour.macros.core.MacrosPersistable.NO_ID;

public class Schema {

    public static final String ID_COLUMN_NAME = "id";
    public static final String CREATE_TIME_COLUMN_NAME = "create_time";
    public static final String MODIFY_TIME_COLUMN_NAME = "modify_time";

    private Schema() {
    }

    private static <M> Column<M, Long> idColumn() {
        return builder(ID_COLUMN_NAME, Types.ID).defaultValue(NO_ID).notNull().unique().notEditable().build();
    }

    private static <M> Column<M, Long> createTimeColumn() {
        return builder(CREATE_TIME_COLUMN_NAME, Types.TIMESTAMP).defaultValue(0L).notEditable().build();
    }

    private static <M> Column<M, Long> modifyTimeColumn() {
        return builder(MODIFY_TIME_COLUMN_NAME, Types.TIMESTAMP).defaultValue(0L).notEditable().build();
    }

    private static <J> ColumnImpl.Builder<J> builder(String name, MacrosType<J> type) {
        return new ColumnImpl.Builder<>(name, type);
    }

    /*
     * CAREFUL: The 'INSTANCE = new ...' line needs to come at the end of all static initialisers,
     * due to static initialisation ordering matching line ordering in the source file.
     * -> Solution: lazy initialisation of instances
     */

    public final static class QtyUnitTable extends BaseTable<QtyUnit> {
        public static final Column<QtyUnit, Long> ID;
        public static final Column<QtyUnit, Long> CREATE_TIME;
        public static final Column<QtyUnit, Long> MODIFY_TIME;
        public static final Column<QtyUnit, String> NAME;
        public static final Column<QtyUnit, String> ABBREVIATION;
        public static final Column<QtyUnit, Boolean> IS_VOLUME_UNIT;
        public static final Column<QtyUnit, Double> METRIC_EQUIVALENT;
        private static final String TABLE_NAME = "QtyUnit";
        private static QtyUnitTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            NAME = builder("name", Types.TEXT).notNull().build();
            ABBREVIATION = builder("abbreviation", Types.TEXT).notNull().inSecondaryKey().unique().build();
            IS_VOLUME_UNIT = builder("is_volume_unit", Types.BOOLEAN).notNull().build();
            METRIC_EQUIVALENT = builder("metric_equivalent", Types.REAL).notNull().build();
        }
        private QtyUnitTable() {
            super(TABLE_NAME, QtyUnit.factory(), ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
                    NAME
                    , ABBREVIATION
                    , IS_VOLUME_UNIT
                    , METRIC_EQUIVALENT
            ));
        }
        public static QtyUnitTable instance() {
            // avoid circular references with static initialisers, caused as the QtyUnit class
            // uses static initialisation of the built in units
            if (INSTANCE == null) {
                INSTANCE = new QtyUnitTable();
            }
            return INSTANCE;
        }
    }

    public final static class FoodTable extends BaseTable<Food> {
        public static final Column<Food, Long> ID;
        public static final Column<Food, Long> CREATE_TIME;
        public static final Column<Food, Long> MODIFY_TIME;
        public static final Column<Food, String> INDEX_NAME;
        public static final Column<Food, String> BRAND;
        public static final Column<Food, String> VARIETY;
        public static final Column<Food, Boolean> VARIETY_AFTER_NAME;
        public static final Column<Food, String> NAME;
        public static final Column<Food, String> NOTES;
        public static final Column<Food, String> FOOD_TYPE;
        public static final Column<Food, Long> USDA_INDEX;
        public static final Column<Food, String> NUTTAB_INDEX;
        public static final Column.Fk<Food, String, FoodCategory> CATEGORY;
        private static final String TABLE_NAME = "Food";
        private static final FoodTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            INDEX_NAME = builder("index_name", Types.TEXT).notNull().inSecondaryKey().unique().build();
            BRAND = builder("brand", Types.TEXT).build();
            VARIETY = builder("variety", Types.TEXT).build();
            VARIETY_AFTER_NAME = builder("variety_after_name", Types.BOOLEAN).notNull().defaultValue(false).build();
            NAME = builder("name", Types.TEXT).notNull().build();
            NOTES = builder("notes", Types.TEXT).build();
            FOOD_TYPE = builder("food_type", Types.TEXT).notEditable().notNull().defaultValue(FoodType.PRIMARY.getName()).build();
            USDA_INDEX = builder("usda_index", Types.INTEGER).notEditable().build();
            NUTTAB_INDEX = builder("nuttab_index", Types.TEXT).notEditable().build();
            CATEGORY = builder("category", Types.TEXT).notEditable().notNull()
                        .buildFk(FoodCategoryTable.NAME, FoodCategoryTable.instance());
            INSTANCE = new FoodTable();
        }
        private FoodTable() {
            super(TABLE_NAME, Food.factory(), ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
                    INDEX_NAME
                    , BRAND
                    , VARIETY
                    , NAME
                    , VARIETY_AFTER_NAME
                    , NOTES
                    , CATEGORY
                    , FOOD_TYPE
                    , USDA_INDEX
                    , NUTTAB_INDEX
            ));
        }
        public static FoodTable instance() {
            return INSTANCE;
        }
    }

    public final static class ServingTable extends BaseTable<Serving> {
        public static final Column<Serving, Long> ID;
        public static final Column<Serving, Long> CREATE_TIME;
        public static final Column<Serving, Long> MODIFY_TIME;
        public static final Column<Serving, String> NAME;
        public static final Column<Serving, Double> QUANTITY;
        public static final Column<Serving, Boolean> IS_DEFAULT;
        public static final Column.Fk<Serving, Long, Food> FOOD_ID;
        public static final Column.Fk<Serving, String, QtyUnit> QUANTITY_UNIT;
        private static final String TABLE_NAME = "Serving";
        private static final ServingTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            NAME = builder("name", Types.TEXT).notNull().build();
            QUANTITY = builder("quantity", Types.REAL).notNull().build();
            IS_DEFAULT = builder("is_default", Types.NULLBOOLEAN).notNull().defaultValue(false).build();
            FOOD_ID = builder("food_id", Types.ID).notEditable().notNull().defaultValue(NO_ID)
                    .buildFk(FoodTable.ID, FoodTable.instance());
            QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notNull()
                    .buildFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance());
            INSTANCE = new ServingTable();
        }
        ServingTable() {
            super(TABLE_NAME, Serving.factory(), ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
                    NAME
                    , QUANTITY
                    , QUANTITY_UNIT
                    , IS_DEFAULT
                    , FOOD_ID
            ));
        }

        public static ServingTable instance() {
            return INSTANCE;
        }
    }

    public final static class FoodPortionTable extends BaseTable<FoodPortion> {
        public static final Column<FoodPortion, Long> ID;
        public static final Column<FoodPortion, Long> CREATE_TIME;
        public static final Column<FoodPortion, Long> MODIFY_TIME;
        public static final Column<FoodPortion, Double> QUANTITY;
        public static final Column.Fk<FoodPortion, String, QtyUnit> QUANTITY_UNIT;
        public static final Column.Fk<FoodPortion, Long, Food> FOOD_ID;
        public static final Column.Fk<FoodPortion, Long, Meal> MEAL_ID;
        public static final Column.Fk<FoodPortion, Long, Serving> SERVING_ID;
        public static final Column<FoodPortion, String> NOTES;
        private static final String TABLE_NAME = "FoodPortion";
        private static final FoodPortionTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            QUANTITY = builder("quantity", Types.REAL).notNull().build();
            NOTES = builder("notes", Types.TEXT).build();
            FOOD_ID = builder("food_id", Types.ID).notEditable().notNull()
                    .buildFk(FoodTable.ID, FoodTable.instance());
            MEAL_ID = builder("meal_id", Types.ID).notEditable().notNull()
                    .buildFk(MealTable.ID, MealTable.instance());
            SERVING_ID = builder("serving_id", Types.ID).notEditable()
                    .buildFk(ServingTable.ID, ServingTable.instance());
            QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notEditable().notNull()
                    .buildFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance());
            INSTANCE = new FoodPortionTable();
        }
        private FoodPortionTable() {
            super(TABLE_NAME, FoodPortion.factory(), ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
                    QUANTITY
                    , QUANTITY_UNIT
                    , FOOD_ID
                    , MEAL_ID
                    , SERVING_ID
                    , NOTES
            ));
        }

        public static FoodPortionTable instance() {
            return INSTANCE;
        }
    }

    public final static class MealTable extends BaseTable<Meal> {
        public static final Column<Meal, Long> ID;
        public static final Column<Meal, Long> CREATE_TIME;
        public static final Column<Meal, Long> MODIFY_TIME;
        public static final Column<Meal, DateStamp> DAY;
        public static final Column<Meal, String> NAME;
        private static final String TABLE_NAME = "Meal";
        private static final MealTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            DAY = builder("day", Types.DATESTAMP).notNull().build();
            NAME = builder("name", Types.TEXT).notNull().build();
            INSTANCE = new MealTable();
        }
        private MealTable() {
            super(TABLE_NAME, Meal.factory(), ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(NAME, DAY));
        }

        public static MealTable instance() {
            return INSTANCE;
        }
    }

    public final static class FoodCategoryTable extends BaseTable<FoodCategory> {
        public static final Column<FoodCategory, Long> ID;
        public static final Column<FoodCategory, Long> CREATE_TIME;
        public static final Column<FoodCategory, Long> MODIFY_TIME;
        public static final Column<FoodCategory, String> NAME;
        private static final String TABLE_NAME = "FoodCategory";
        private static final FoodCategoryTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            NAME = builder("name", Types.TEXT).notNull().inSecondaryKey().unique().build();
            INSTANCE = new FoodCategoryTable();
        }
        private FoodCategoryTable() {
            super(TABLE_NAME, FoodCategory.factory(), ID, CREATE_TIME, MODIFY_TIME, Collections.singletonList(NAME));
        }

        public static FoodCategoryTable instance() {
            return INSTANCE;
        }
    }

    public final static class IngredientTable extends BaseTable<Ingredient> {
        public static final Column<Ingredient, Long> ID;
        public static final Column<Ingredient, Long> CREATE_TIME;
        public static final Column<Ingredient, Long> MODIFY_TIME;
        public static final Column.Fk<Ingredient, Long, Food> COMPOSITE_FOOD_ID;
        public static final Column.Fk<Ingredient, Long, Food> INGREDIENT_FOOD_ID;
        public static final Column<Ingredient, Double> QUANTITY;
        public static final Column.Fk<Ingredient, String, QtyUnit> QUANTITY_UNIT;
        public static final Column.Fk<Ingredient, Long, Serving> SERVING_ID;
        public static final Column<Ingredient, String> NOTES;
        private static final String TABLE_NAME = "Ingredient";
        private static final IngredientTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            NOTES = builder("notes", Types.TEXT).build();
            COMPOSITE_FOOD_ID = builder("composite_food_id", Types.ID).notEditable().notNull()
                    .defaultValue(NO_ID).buildFk(FoodTable.ID, FoodTable.instance());
            INGREDIENT_FOOD_ID = builder("ingredient_food_id", Types.ID).notEditable().notNull()
                    .defaultValue(NO_ID).buildFk(FoodTable.ID, FoodTable.instance());
            QUANTITY = builder("quantity", Types.REAL).notEditable().notNull().build();
            QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notEditable().notNull()
                    .buildFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance());
            SERVING_ID = builder("serving_id", Types.ID) // nullable
                    .buildFk(ServingTable.ID, ServingTable.instance());
            INSTANCE = new IngredientTable();
        }
        private IngredientTable() {
            super(TABLE_NAME, Ingredient.factory(), ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
                  COMPOSITE_FOOD_ID
                , INGREDIENT_FOOD_ID
                , QUANTITY
                , QUANTITY_UNIT
                , SERVING_ID
                , NOTES
                ));
        }

        public static IngredientTable instance() {
            return INSTANCE;
        }
    }

    public final static class RegularMealTable extends BaseTable<RegularMeal> {
        public static final Column<RegularMeal, Long> ID;
        public static final Column<RegularMeal, Long> CREATE_TIME;
        public static final Column<RegularMeal, Long> MODIFY_TIME;
        public static final Column<RegularMeal, String> NAME;
        public static final Column.Fk<RegularMeal, Long, Meal> MEAL_ID;
        private static final String TABLE_NAME = "RegularMeal";
        private static final RegularMealTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            NAME = builder("name", Types.TEXT).notNull().build();
            MEAL_ID = builder("meal_id", Types.ID).notEditable().notNull().inSecondaryKey().unique()
                    .buildFk(MealTable.ID, MealTable.instance());
            INSTANCE = new RegularMealTable();
        }
        RegularMealTable()  {
            super(TABLE_NAME, RegularMeal.factory(), ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(NAME, MEAL_ID));
        }

        public static RegularMealTable instance() {
            return INSTANCE;
        }
    }

    public final static class NutritionDataTable extends BaseTable<NutritionData> {
        /*
         * When adding new columns here (or to any other table), remember to put
         *  - the field declaration (obviously)
         *  - the static initialiser using the builder (won't compile if you don't do this)
         *  - add it to the list of columns in the Table object's constructor
         *
         * And additionally, for NutritionData, add to NutritionData.NUTRIENT_COLUMNS if appropriate
         */
        public static final Column<NutritionData, Long> ID;
        public static final Column<NutritionData, Long> CREATE_TIME;
        public static final Column<NutritionData, Long> MODIFY_TIME;
        public static final Column<NutritionData, String> DATA_SOURCE;
        public static final Column<NutritionData, Double> QUANTITY;
        public static final Column<NutritionData, Double> DENSITY;
        public static final Column<NutritionData, Double> KILOJOULES;
        public static final Column<NutritionData, Double> CALORIES;
        public static final Column<NutritionData, Double> PROTEIN;
        public static final Column<NutritionData, Double> CARBOHYDRATE;
        public static final Column<NutritionData, Double> CARBOHYDRATE_BY_DIFF;
        public static final Column<NutritionData, Double> SUGAR;
        public static final Column<NutritionData, Double> SUGAR_ALCOHOL;
        public static final Column<NutritionData, Double> STARCH;
        public static final Column<NutritionData, Double> FAT;
        public static final Column<NutritionData, Double> SATURATED_FAT;
        public static final Column<NutritionData, Double> MONOUNSATURATED_FAT;
        public static final Column<NutritionData, Double> POLYUNSATURATED_FAT;
        public static final Column<NutritionData, Double> OMEGA_3_FAT;
        public static final Column<NutritionData, Double> OMEGA_6_FAT;
        public static final Column<NutritionData, Double> FIBRE;
        public static final Column<NutritionData, Double> SODIUM;
        public static final Column<NutritionData, Double> SALT;
        public static final Column<NutritionData, Double> POTASSIUM;
        public static final Column<NutritionData, Double> CALCIUM;
        public static final Column<NutritionData, Double> IRON;
        public static final Column<NutritionData, Double> WATER;
        public static final Column<NutritionData, Double> ALCOHOL;
        public static final Column.Fk<NutritionData, Long, Food> FOOD_ID;
        public static final Column.Fk<NutritionData, String, QtyUnit> QUANTITY_UNIT;
        private static final String TABLE_NAME = "NutritionData";
        private static final NutritionDataTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            DATA_SOURCE = builder("data_source", Types.TEXT).build();
            QUANTITY = builder("quantity", Types.REAL).notNull().defaultValue(100.0).build();
            DENSITY = builder("density", Types.REAL).build();
            KILOJOULES = builder("kilojoules", Types.REAL).build();
            CALORIES = builder("calories", Types.REAL).build();
            PROTEIN = builder("protein", Types.REAL).build();
            CARBOHYDRATE = builder("carbohydrate", Types.REAL).build();
            CARBOHYDRATE_BY_DIFF = builder("carbohydrate_by_diff", Types.REAL).build();
            SUGAR = builder("sugar", Types.REAL).build();
            SUGAR_ALCOHOL = builder("sugar_alcohol", Types.REAL).build();
            STARCH = builder("starch", Types.REAL).build();
            FAT = builder("fat", Types.REAL).build();
            SATURATED_FAT = builder("saturated_fat", Types.REAL).build();
            MONOUNSATURATED_FAT = builder("monounsaturated_fat", Types.REAL).build();
            POLYUNSATURATED_FAT = builder("polyunsaturated_fat", Types.REAL).build();
            OMEGA_3_FAT = builder("omega_3", Types.REAL).build();
            OMEGA_6_FAT = builder("omega_6", Types.REAL).build();
            FIBRE = builder("fibre", Types.REAL).build();
            SODIUM = builder("sodium", Types.REAL).build();
            SALT = builder("salt", Types.REAL).build();
            POTASSIUM = builder("potassium", Types.REAL).build();
            CALCIUM = builder("calcium", Types.REAL).build();
            IRON = builder("iron", Types.REAL).build();
            WATER = builder("water", Types.REAL).build();
            ALCOHOL = builder("alcohol", Types.REAL).build();
            // FOOD_ID can be null for computed instances
            FOOD_ID = builder("food_id", Types.ID).notEditable().defaultValue(NO_ID).inSecondaryKey().unique()
                    .buildFk(FoodTable.ID, FoodTable.instance());
            QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notNull()
                    .buildFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance());
            INSTANCE = new NutritionDataTable();
        }
        private NutritionDataTable() {
            super(TABLE_NAME, NutritionData.factory(), ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
                    FOOD_ID
                    , DATA_SOURCE
                    , QUANTITY
                    , QUANTITY_UNIT
                    , DENSITY
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
                    , POTASSIUM
                    , SALT
                    , CALCIUM
                    , IRON
                    , WATER
                    , ALCOHOL
            ));
        }
        public static NutritionDataTable instance() {
            return INSTANCE;
        }
    }

    public final static class FoodAttributeTable extends BaseTable<FoodAttribute> {
        public static final Column<FoodAttribute, Long> ID;
        public static final Column<FoodAttribute, Long> CREATE_TIME;
        public static final Column<FoodAttribute, Long> MODIFY_TIME;
        public static final Column<FoodAttribute, String> NAME;
        private static final String TABLE_NAME = "FoodAttribute";
        private static final FoodAttributeTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            NAME = builder("name", Types.TEXT).notNull().inSecondaryKey().unique().build();
            INSTANCE = new FoodAttributeTable();
        }
        private FoodAttributeTable() {
            super(TABLE_NAME, FoodAttribute.factory(), ID, CREATE_TIME, MODIFY_TIME, Collections.singletonList(NAME));
        }
        public static FoodAttributeTable instance() {
            return INSTANCE;
        }
    }

    public final static class AttrMappingTable extends BaseTable<AttrMapping> {
        public static final Column<AttrMapping, Long> ID;
        public static final Column<AttrMapping, Long> CREATE_TIME;
        public static final Column<AttrMapping, Long> MODIFY_TIME;
        public static final Column.Fk<AttrMapping, Long, Food> FOOD_ID;
        public static final Column.Fk<AttrMapping, Long, FoodAttribute> ATTRIBUTE_ID;
        private static final String TABLE_NAME = "AttributeMapping";
        private static final AttrMappingTable INSTANCE;

        static {
            ID = idColumn();
            CREATE_TIME = createTimeColumn();
            MODIFY_TIME = modifyTimeColumn();
            FOOD_ID = builder("food_id", Types.ID).notEditable().notNull().inSecondaryKey()
                    .buildFk(FoodTable.ID, FoodTable.instance());
            ATTRIBUTE_ID = builder("attribute_id", Types.ID).notEditable().notNull().inSecondaryKey()
                    .buildFk(FoodAttributeTable.ID, FoodAttributeTable.instance());
            INSTANCE = new AttrMappingTable();
        }
        private AttrMappingTable() {
            super(TABLE_NAME, AttrMapping.factory(), ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(FOOD_ID , ATTRIBUTE_ID));
        }
        public static AttrMappingTable instance() {
            return INSTANCE;
        }
    }
}
