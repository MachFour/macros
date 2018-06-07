package com.machfour.macros.data;

import com.machfour.macros.core.*;
import com.machfour.macros.util.DateStamp;

import java.util.Arrays;
import java.util.Collections;

import static com.machfour.macros.core.MacrosPersistable.NO_ID;
import static com.machfour.macros.data.Column.builder;
import static com.machfour.macros.data.Types.*;

public class Schema {

    public static final String ID_COLUMN_NAME = "id";
    public static final String CREATE_TIME_COLUMN_NAME = "create_time";
    public static final String MODIFY_TIME_COLUMN_NAME = "modify_time";

    private Schema() {
    }

    private static <M> Column<M, Long> idColumn(int index) {
        return builder(ID_COLUMN_NAME, ID, index).defaultValue(NO_ID).build();
    }

    private static <M> Column<M, Long> createTimeColumn(int index) {
        return builder(CREATE_TIME_COLUMN_NAME, TIMESTAMP, index).defaultValue(0L).build();
    }

    private static <M> Column<M, Long> modifyTimeColumn(int index) {
        return builder(MODIFY_TIME_COLUMN_NAME, TIMESTAMP, index).defaultValue(0L).build();
    }

    /*
     * CAREFUL: The 'INSTANCE = new ...' line needs to come at the end of all static initialisers,
     * due to static initialisation ordering matching line ordering in the source file.
     */

    public final static class QuantityUnitTable extends BaseTable<QuantityUnit> {
        public static final Column<QuantityUnit, Long> ID;
        public static final Column<QuantityUnit, Long> CREATE_TIME;
        public static final Column<QuantityUnit, Long> MODIFY_TIME;
        public static final Column<QuantityUnit, String> NAME;
        public static final Column<QuantityUnit, String> ABBREVIATION;
        public static final Column<QuantityUnit, Boolean> IS_VOLUME_UNIT;
        public static final Column<QuantityUnit, Double> METRIC_EQUIVALENT;
        private static final String TABLE_NAME = "QuantityUnit";
        private static final QuantityUnitTable INSTANCE;

        static {
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            NAME = builder("name", TEXT, columnIndex++).notNull().build();
            ABBREVIATION = builder("abbreviation", TEXT, columnIndex++).notNull().inSecondaryKey().build();
            IS_VOLUME_UNIT = builder("is_volume_unit", BOOLEAN, columnIndex++).notNull().build();
            METRIC_EQUIVALENT = builder("metric_equivalent", REAL, columnIndex++).notNull().build();

            INSTANCE = new QuantityUnitTable();
        }
        QuantityUnitTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
                    NAME
                    , ABBREVIATION
                    , IS_VOLUME_UNIT
                    , METRIC_EQUIVALENT
            ));
        }

        public static QuantityUnitTable instance() {
            return INSTANCE;
        }

        public QuantityUnit construct(ColumnData<QuantityUnit> dataMap, ObjectSource objectSource) {
            return new QuantityUnit(dataMap, objectSource);
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
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            INDEX_NAME = builder("index_name", TEXT, columnIndex++).notNull().inSecondaryKey().build();
            BRAND = builder("brand", TEXT, columnIndex++).build();
            VARIETY = builder("variety", TEXT, columnIndex++).build();
            VARIETY_AFTER_NAME = builder("variety_after_name", BOOLEAN, columnIndex++).notNull().build();
            NAME = builder("name", TEXT, columnIndex++).notNull().build();
            NOTES = builder("notes", TEXT, columnIndex++).build();
            FOOD_TYPE = builder("food_type", TEXT, columnIndex++).notEditable().notNull().defaultValue(FoodType.PRIMARY.getName()).build();
            USDA_INDEX = builder("usda_index", INTEGER, columnIndex++).notEditable().build();
            NUTTAB_INDEX = builder("nuttab_index", TEXT, columnIndex++).notEditable().build();
            CATEGORY = builder("category", TEXT, columnIndex++).notEditable().notNull()
                        .buildFk(FoodCategoryTable.NAME, FoodCategoryTable.instance());

            INSTANCE = new FoodTable();
        }

        FoodTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
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

        public Food construct(ColumnData<Food> dataMap, ObjectSource objectSource) {
            return new Food(dataMap, objectSource);
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
        public static final Column.Fk<Serving, String, QuantityUnit> QUANTITY_UNIT;
        private static final String TABLE_NAME = "Serving";
        private static final ServingTable INSTANCE;

        static {
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            NAME = builder("name", TEXT, columnIndex++).notNull().build();
            QUANTITY = builder("quantity", REAL, columnIndex++).notNull().build();
            IS_DEFAULT = builder("is_default", NULLBOOLEAN, columnIndex++).notNull().defaultValue(false).build();
            FOOD_ID = builder("food_id", Types.ID, columnIndex++).notEditable().notNull().defaultValue(NO_ID)
                    .buildFk(FoodTable.ID, FoodTable.instance());
            QUANTITY_UNIT = builder("quantity_unit", TEXT, columnIndex++).notNull()
                    .buildFk(QuantityUnitTable.ABBREVIATION, QuantityUnitTable.instance());

            INSTANCE = new ServingTable();
        }
        ServingTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
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

        public Serving construct(ColumnData<Serving> dataMap, ObjectSource objectSource) {
            return new Serving(dataMap, objectSource);
        }
    }

    public final static class FoodPortionTable extends BaseTable<FoodPortion> {
        public static final Column<FoodPortion, Long> ID;
        public static final Column<FoodPortion, Long> CREATE_TIME;
        public static final Column<FoodPortion, Long> MODIFY_TIME;
        public static final Column<FoodPortion, Double> QUANTITY;
        public static final Column.Fk<FoodPortion, Long, QuantityUnit> QUANTITY_UNIT;
        public static final Column.Fk<FoodPortion, Long, Food> FOOD_ID;
        public static final Column.Fk<FoodPortion, Long, Meal> MEAL_ID;
        public static final Column.Fk<FoodPortion, Long, Serving> SERVING_ID;
        public static final Column<FoodPortion, String> NOTES;
        private static final String TABLE_NAME = "FoodPortion";
        private static final FoodPortionTable INSTANCE;

        static {
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            QUANTITY = builder("quantity", REAL, columnIndex++).notNull().build();
            NOTES = builder("notes", TEXT, columnIndex++).notNull().build();
            FOOD_ID = builder("food_id", Types.ID, columnIndex++).notEditable().notNull()
                    .buildFk(FoodTable.ID, FoodTable.instance());
            MEAL_ID = builder("meal_id", Types.ID, columnIndex++).notEditable().notNull()
                    .buildFk(MealTable.ID, MealTable.instance());
            SERVING_ID = builder("serving_id", Types.ID, columnIndex++).notEditable().notNull()
                    .buildFk(ServingTable.ID, ServingTable.instance());
            QUANTITY_UNIT = builder("quantity_unit", Types.ID, columnIndex++).notEditable().notNull()
                    .buildFk(QuantityUnitTable.ID, QuantityUnitTable.instance());


            INSTANCE = new FoodPortionTable();
        }
        FoodPortionTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
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

        public FoodPortion construct(ColumnData<FoodPortion> dataMap, ObjectSource objectSource) {
            return new FoodPortion(dataMap, objectSource);
        }
    }

    public final static class MealTable extends BaseTable<Meal> {
        public static final Column<Meal, Long> ID;
        public static final Column<Meal, Long> CREATE_TIME;
        public static final Column<Meal, Long> MODIFY_TIME;
        public static final Column<Meal, DateStamp> DAY;
        public static final Column.Fk<Meal, String, MealDescription> DESCRIPTION;
        private static final String TABLE_NAME = "Meal";
        private static final MealTable INSTANCE;

        static {
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            DAY = builder("name", DATESTAMP, columnIndex++).notNull().build();
            DESCRIPTION = builder("name", TEXT, columnIndex++).notNull()
                    .buildFk(MealDescriptionTable.NAME, MealDescriptionTable.instance());

            INSTANCE = new MealTable();
        }
        MealTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(DESCRIPTION, DAY));
        }

        public static MealTable instance() {
            return INSTANCE;
        }

        public Meal construct(ColumnData<Meal> dataMap, ObjectSource objectSource) {
            return new Meal(dataMap, objectSource);
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
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            NAME = builder("name", TEXT, columnIndex++).notNull().inSecondaryKey().build();

            INSTANCE = new FoodCategoryTable();

        }
        FoodCategoryTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Collections.singletonList(NAME));
        }

        public static FoodCategoryTable instance() {
            return INSTANCE;
        }

        public FoodCategory construct(ColumnData<FoodCategory> dataMap, ObjectSource objectSource) {
            return new FoodCategory(dataMap, objectSource);
        }
    }

    public final static class MealDescriptionTable extends BaseTable<MealDescription> {
        public static final Column<MealDescription, Long> ID;
        public static final Column<MealDescription, Long> CREATE_TIME;
        public static final Column<MealDescription, Long> MODIFY_TIME;
        public static final Column<MealDescription, String> NAME;
        private static final String TABLE_NAME = "MealDescription";
        private static final MealDescriptionTable INSTANCE;

        static {
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            NAME = builder("name", TEXT, columnIndex++).notNull().inSecondaryKey().build();

            INSTANCE = new MealDescriptionTable();
        }
        MealDescriptionTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Collections.singletonList(NAME));
        }

        public static MealDescriptionTable instance() {
            return INSTANCE;
        }

        public MealDescription construct(ColumnData<MealDescription> dataMap, ObjectSource objectSource) {
            return new MealDescription(dataMap, objectSource);
        }
    }

    public final static class IngredientTable extends BaseTable<Ingredient> {
        public static final Column<Ingredient, Long> ID;
        public static final Column<Ingredient, Long> CREATE_TIME;
        public static final Column<Ingredient, Long> MODIFY_TIME;
        public static final Column.Fk<Ingredient, Long, Food> COMPOSITE_FOOD_ID;
        public static final Column.Fk<Ingredient, Long, Food> INGREDIENT_FOOD_ID;
        public static final Column<Ingredient, Double> QUANTITY;
        public static final Column.Fk<Ingredient, Long, QuantityUnit> QUANTITY_UNIT;
        public static final Column.Fk<Ingredient, Long, Serving> SERVING_ID;
        public static final Column<Ingredient, String> NOTES;
        private static final String TABLE_NAME = "Ingredient";
        private static final IngredientTable INSTANCE;

        static {
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            NOTES = builder("notes", TEXT, columnIndex++).build();
            COMPOSITE_FOOD_ID = builder("composite_food_id", Types.ID, columnIndex++).notEditable().notNull()
                    .buildFk(FoodTable.ID, FoodTable.instance());
            INGREDIENT_FOOD_ID = builder("ingredient_food_id", Types.ID, columnIndex++).notEditable().notNull()
                    .buildFk(FoodTable.ID, FoodTable.instance());
            QUANTITY = builder("quantity", REAL, columnIndex++).notEditable().notNull().build();
            QUANTITY_UNIT = builder("quantity_unit", Types.ID, columnIndex++).notEditable().notNull()
                    .buildFk(QuantityUnitTable.ID, QuantityUnitTable.instance());
            SERVING_ID = builder("serving_id", Types.ID, columnIndex++).notNull()
                    .buildFk(ServingTable.ID, ServingTable.instance());

            INSTANCE = new IngredientTable();
        }
        IngredientTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
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

        public Ingredient construct(ColumnData<Ingredient> dataMap, ObjectSource objectSource) {
            return new Ingredient(dataMap, objectSource);
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
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            NAME = builder("name", TEXT, columnIndex++).notNull().build();
            MEAL_ID = builder("meal_id", Types.ID, columnIndex++).notEditable().notNull().inSecondaryKey()
                    .buildFk(MealTable.ID, MealTable.instance());
            INSTANCE = new RegularMealTable();
        }
        RegularMealTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(NAME, MEAL_ID));
        }

        public static RegularMealTable instance() {
            return INSTANCE;
        }

        public RegularMeal construct(ColumnData<RegularMeal> dataMap, ObjectSource objectSource) {
            return new RegularMeal(dataMap, objectSource);
        }
    }

    public final static class NutritionDataTable extends BaseTable<NutritionData> {
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
        public static final Column<NutritionData, Double> CALCIUM;
        public static final Column<NutritionData, Double> WATER;
        public static final Column<NutritionData, Double> ALCOHOL;
        public static final Column.Fk<NutritionData, Long, Food> FOOD_ID;
        public static final Column.Fk<NutritionData, String, QuantityUnit> QUANTITY_UNIT;
        private static final String TABLE_NAME = "NutritionData";
        private static final NutritionDataTable INSTANCE;

        static {
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            DATA_SOURCE = builder("data_source", TEXT, columnIndex++).build();
            QUANTITY = builder("quantity", REAL, columnIndex++).notNull().defaultValue(100.0).build();
            DENSITY = builder("density", REAL, columnIndex++).build();
            KILOJOULES = builder("kilojoules", REAL, columnIndex++).build();
            CALORIES = builder("calories", REAL, columnIndex++).build();
            PROTEIN = builder("protein", REAL, columnIndex++).build();
            CARBOHYDRATE = builder("carbohydrate", REAL, columnIndex++).build();
            CARBOHYDRATE_BY_DIFF = builder("carbohydrate_by_diff", REAL, columnIndex++).build();
            SUGAR = builder("sugar", REAL, columnIndex++).build();
            SUGAR_ALCOHOL = builder("sugar_alcohol", REAL, columnIndex++).build();
            STARCH = builder("starch", REAL, columnIndex++).build();
            FAT = builder("fat", REAL, columnIndex++).build();
            SATURATED_FAT = builder("saturated_fat", REAL, columnIndex++).build();
            MONOUNSATURATED_FAT = builder("monounsaturated_fat", REAL, columnIndex++).build();
            POLYUNSATURATED_FAT = builder("polyunsaturated_fat", REAL, columnIndex++).build();
            OMEGA_3_FAT = builder("omega_3", REAL, columnIndex++).build();
            OMEGA_6_FAT = builder("omega_6", REAL, columnIndex++).build();
            FIBRE = builder("fibre", REAL, columnIndex++).build();
            SODIUM = builder("sodium", REAL, columnIndex++).build();
            SALT = builder("salt", REAL, columnIndex++).build();
            CALCIUM = builder("calcium", REAL, columnIndex++).build();
            WATER = builder("water", REAL, columnIndex++).build();
            ALCOHOL = builder("alcohol", REAL, columnIndex++).build();
            FOOD_ID = builder("food_id", Types.ID, columnIndex++).notEditable().notNull().defaultValue(NO_ID).inSecondaryKey()
                    .buildFk(FoodTable.ID, FoodTable.instance());
            QUANTITY_UNIT = builder("quantity_unit", Types.TEXT, columnIndex++).notEditable().notNull()
                    .buildFk(QuantityUnitTable.ABBREVIATION, QuantityUnitTable.instance());

            INSTANCE = new NutritionDataTable();
        }
        NutritionDataTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(
                    FOOD_ID
                    , DATA_SOURCE
                    , QUANTITY
                    , DENSITY
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
            ));
        }

        public static NutritionDataTable instance() {
            return INSTANCE;
        }

        public NutritionData construct(ColumnData<NutritionData> dataMap, ObjectSource objectSource) {
            return new NutritionData(dataMap, objectSource);
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
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            NAME = builder("name", TEXT, columnIndex++).notNull().inSecondaryKey().build();

            INSTANCE = new FoodAttributeTable();
        }
        FoodAttributeTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Collections.singletonList(NAME));
        }

        public static FoodAttributeTable instance() {
            return INSTANCE;
        }

        public FoodAttribute construct(ColumnData<FoodAttribute> dataMap, ObjectSource objectSource) {
            return new FoodAttribute(dataMap, objectSource);
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
            int columnIndex = 0;
            ID = idColumn(columnIndex++);
            CREATE_TIME = createTimeColumn(columnIndex++);
            MODIFY_TIME = modifyTimeColumn(columnIndex++);
            FOOD_ID = builder("food_id", Types.ID, columnIndex++).notEditable().notNull().inSecondaryKey()
                    .buildFk(FoodTable.ID, FoodTable.instance());
            ATTRIBUTE_ID = builder("attribute_id", Types.ID, columnIndex++).notEditable().notNull().inSecondaryKey()
                    .buildFk(FoodAttributeTable.ID, FoodAttributeTable.instance());
            INSTANCE = new AttrMappingTable();
        }
        AttrMappingTable() {
            super(TABLE_NAME, ID, CREATE_TIME, MODIFY_TIME, Arrays.asList(FOOD_ID , ATTRIBUTE_ID));
        }

        public static AttrMappingTable instance() {
            return INSTANCE;
        }

        public AttrMapping construct(ColumnData<AttrMapping> dataMap, ObjectSource objectSource) {
            return new AttrMapping(dataMap, objectSource);
        }
    }
}
