package com.machfour.macros.core;

import com.machfour.macros.core.datatype.MacrosType;
import com.machfour.macros.core.datatype.Types;
import com.machfour.macros.objects.*;
import com.machfour.macros.util.DateStamp;

import java.util.ArrayList;
import java.util.List;

import static com.machfour.macros.core.MacrosEntity.NO_ID;

// @formatter:off

/**
 *  Class holding the database column information for all persistable entities used in this library
 *  You will know very quickly if you break anything in this class, because basically everything uses
 *  the tables and columns defined here.
 */
public class Schema {
    private Schema() { }

    static final String ID_COLUMN_NAME = "id";
    static final String CREATE_TIME_COLUMN_NAME = "create_time";
    static final String MODIFY_TIME_COLUMN_NAME = "modify_time";



    private static <M> Column<M, Long> idColumnBuildAndAdd(List<Column<M, ?>> columnList) {
        return builder(ID_COLUMN_NAME, Types.ID).defaultsTo(NO_ID).notNull().unique().notEditable()
                .buildAndAdd(columnList);
    }

    private static <M> Column<M, Long> createTimeColumnBuildAndAdd(List<Column<M, ?>> columnList) {
        return builder(CREATE_TIME_COLUMN_NAME, Types.TIMESTAMP).defaultsTo(0L).notEditable().buildAndAdd(columnList);
    }

    private static <M> Column<M, Long> modifyTimeColumnBuildAndAdd(List<Column<M, ?>> columnList) {
        return builder(MODIFY_TIME_COLUMN_NAME, Types.TIMESTAMP).defaultsTo(0L).notEditable()
                .buildAndAdd(columnList);
    }

    private static <J> ColumnImpl.Builder<J> builder(String name, MacrosType<J> type) {
        return new ColumnImpl.Builder<>(name, type);
    }

    public final static class QtyUnitTable extends BaseTable<QtyUnit> {
        private static final String TABLE_NAME = "QtyUnit";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<QtyUnit, ?>> COLUMNS = new ArrayList<>();

        // initialisation order here is the order of iteration of the columns
        public static final Column<QtyUnit, Long> ID;
        public static final Column<QtyUnit, Long> CREATE_TIME;
        public static final Column<QtyUnit, Long> MODIFY_TIME;
        public static final Column<QtyUnit, String> NAME;
        public static final Column<QtyUnit, String> ABBREVIATION;
        public static final Column<QtyUnit, Boolean> IS_VOLUME_UNIT;
        public static final Column<QtyUnit, Double> METRIC_EQUIVALENT;

        static {
            ID                = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME       = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME       = modifyTimeColumnBuildAndAdd(COLUMNS);
            NAME              = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS);
            ABBREVIATION      = builder("abbreviation", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS);
            IS_VOLUME_UNIT    = builder("is_volume_unit", Types.BOOLEAN).notNull().buildAndAdd(COLUMNS);
            METRIC_EQUIVALENT = builder("metric_equivalent", Types.REAL).notNull().buildAndAdd(COLUMNS);
        }

        // this declaration has to come last (static initialisation order
        private static final QtyUnitTable INSTANCE = new QtyUnitTable();

        public static QtyUnitTable instance() {
            return INSTANCE;
        }

        private QtyUnitTable() {
            // QtyUnit.factory() causes initialisation of QtyUnit, which depends on this class.
            // So the columns are initialised as a side effect of calling this function.
            super(TABLE_NAME, QtyUnit.factory(), COLUMNS);
        }
    }

    public final static class FoodTable extends BaseTable<Food> {
        private static final String TABLE_NAME = "Food";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<Food, ?>> COLUMNS = new ArrayList<>();

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

        static {
            // order of initialisation is order that columns will be iterated through
            ID                 = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME        = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME        = modifyTimeColumnBuildAndAdd(COLUMNS);
            INDEX_NAME         = builder("index_name", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS);
            NAME               = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS);
            BRAND              = builder("brand", Types.TEXT).buildAndAdd(COLUMNS);
            VARIETY            = builder("variety", Types.TEXT).buildAndAdd(COLUMNS);
            VARIETY_AFTER_NAME = builder("variety_after_name", Types.BOOLEAN).notNull()
                                    .defaultsTo(false).buildAndAdd(COLUMNS);
            NOTES              = builder("notes", Types.TEXT).buildAndAdd(COLUMNS);
            CATEGORY           = builder("category", Types.TEXT).notNull()
                                    .buildAndAddFk(FoodCategoryTable.NAME, FoodCategoryTable.instance(), COLUMNS);
            FOOD_TYPE          = builder("food_type", Types.TEXT).notEditable().notNull()
                                    .defaultsTo(FoodType.PRIMARY.getName()).buildAndAdd(COLUMNS);
            USDA_INDEX         = builder("usda_index", Types.INTEGER).notEditable().buildAndAdd(COLUMNS);
            NUTTAB_INDEX       = builder("nuttab_index", Types.TEXT).notEditable().buildAndAdd(COLUMNS);
        }

        // this has to come last (static initialisation order)
        private static final FoodTable INSTANCE = new FoodTable();

        public static FoodTable instance() {
            return INSTANCE;
        }
        private FoodTable() {
            super(TABLE_NAME, Food.factory(), COLUMNS);
        }

    }

    public final static class ServingTable extends BaseTable<Serving> {
        private static final String TABLE_NAME = "Serving";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<Serving, ?>> COLUMNS = new ArrayList<>();

        public static final Column<Serving, Long> ID;
        public static final Column<Serving, Long> CREATE_TIME;
        public static final Column<Serving, Long> MODIFY_TIME;
        public static final Column<Serving, String> NAME;
        public static final Column<Serving, Double> QUANTITY;
        public static final Column<Serving, Boolean> IS_DEFAULT;
        public static final Column.Fk<Serving, Long, Food> FOOD_ID;
        public static final Column.Fk<Serving, String, QtyUnit> QUANTITY_UNIT;

        static {
            ID            = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME   = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME   = modifyTimeColumnBuildAndAdd(COLUMNS);
            NAME          = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS);
            QUANTITY      = builder("quantity", Types.REAL).notNull().buildAndAdd(COLUMNS);
            QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notNull()
                            .buildAndAddFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance(), COLUMNS);
            IS_DEFAULT    = builder("is_default", Types.NULLBOOLEAN).notNull().defaultsTo(false).buildAndAdd(COLUMNS);
            FOOD_ID       = builder("food_id", Types.ID).notEditable().notNull().defaultsTo(NO_ID)
                              .buildAndAddFk(FoodTable.ID, FoodTable.instance(), COLUMNS);
        }

        // this declaration has to be last (static initialisation order)
        private static final ServingTable INSTANCE = new ServingTable();


        public static ServingTable instance() {
            return INSTANCE;
        }

        ServingTable() {
            super(TABLE_NAME, Serving.factory(), COLUMNS);
        }
    }

    public final static class FoodPortionTable extends BaseTable<FoodPortion> {
        private static final String TABLE_NAME = "FoodPortion";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<FoodPortion, ?>> COLUMNS = new ArrayList<>();

        public static final Column<FoodPortion, Long> ID;
        public static final Column<FoodPortion, Long> CREATE_TIME;
        public static final Column<FoodPortion, Long> MODIFY_TIME;
        public static final Column<FoodPortion, Double> QUANTITY;
        public static final Column.Fk<FoodPortion, String, QtyUnit> QUANTITY_UNIT;
        public static final Column.Fk<FoodPortion, Long, Food> FOOD_ID;
        public static final Column.Fk<FoodPortion, Long, Meal> MEAL_ID;
        public static final Column.Fk<FoodPortion, Long, Serving> SERVING_ID;
        public static final Column<FoodPortion, String> NOTES;

        static {
            ID            = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME   = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME   = modifyTimeColumnBuildAndAdd(COLUMNS);
            QUANTITY      = builder("quantity", Types.REAL).notNull().buildAndAdd(COLUMNS);
            QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notEditable().notNull()
                                .buildAndAddFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance(), COLUMNS);
            FOOD_ID       = builder("food_id", Types.ID).notEditable().notNull()
                                .buildAndAddFk(FoodTable.ID, FoodTable.instance(), COLUMNS);
            MEAL_ID       = builder("meal_id", Types.ID).notEditable().notNull()
                                .buildAndAddFk(MealTable.ID, MealTable.instance(), COLUMNS);
            SERVING_ID    = builder("serving_id", Types.ID).notEditable()
                                .buildAndAddFk(ServingTable.ID, ServingTable.instance(), COLUMNS);
            NOTES         = builder("notes", Types.TEXT).buildAndAdd(COLUMNS);
        }


        private static final FoodPortionTable INSTANCE = new FoodPortionTable();
        private FoodPortionTable() {
            super(TABLE_NAME, FoodPortion.factory(), COLUMNS);
        }

        public static FoodPortionTable instance() {
            return INSTANCE;
        }
    }

    public final static class MealTable extends BaseTable<Meal> {
        private static final String TABLE_NAME = "Meal";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<Meal, ?>> COLUMNS = new ArrayList<>();

        public static final Column<Meal, Long> ID;
        public static final Column<Meal, Long> CREATE_TIME;
        public static final Column<Meal, Long> MODIFY_TIME;
        public static final Column<Meal, String> NAME;
        public static final Column<Meal, DateStamp> DAY;
        public static final Column<Meal, Long> START_TIME;
        public static final Column<Meal, Long> DURATION;

        static {
            ID          = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS);
            NAME        = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS);
            DAY         = builder("day", Types.DATESTAMP).notNull().buildAndAdd(COLUMNS);
            START_TIME  = builder("time", Types.TIMESTAMP).notNull().defaultsTo(0L).buildAndAdd(COLUMNS);
            DURATION    = builder("duration", Types.INTEGER).notNull().defaultsTo(0L).buildAndAdd(COLUMNS);
        }

        private static final MealTable INSTANCE = new MealTable();

        public static MealTable instance() {
            return INSTANCE;
        }

        private MealTable() {
            super(TABLE_NAME, Meal.factory(), COLUMNS);
        }
    }

    public final static class FoodCategoryTable extends BaseTable<FoodCategory> {
        private static final String TABLE_NAME = "FoodCategory";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<FoodCategory, ?>> COLUMNS = new ArrayList<>();

        public static final Column<FoodCategory, Long> ID;
        public static final Column<FoodCategory, Long> CREATE_TIME;
        public static final Column<FoodCategory, Long> MODIFY_TIME;
        public static final Column<FoodCategory, String> NAME;

        static {
            ID = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS);
            NAME = builder("name", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS);
        }

        private static final FoodCategoryTable INSTANCE = new FoodCategoryTable();

        public static FoodCategoryTable instance() {
            return INSTANCE;
        }

        private FoodCategoryTable() {
            super(TABLE_NAME, FoodCategory.factory(), COLUMNS);
        }

    }

    public final static class IngredientTable extends BaseTable<Ingredient> {
        private static final String TABLE_NAME = "Ingredient";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<Ingredient, ?>> COLUMNS = new ArrayList<>();

        public static final Column<Ingredient, Long> ID;
        public static final Column<Ingredient, Long> CREATE_TIME;
        public static final Column<Ingredient, Long> MODIFY_TIME;
        public static final Column.Fk<Ingredient, Long, Food> COMPOSITE_FOOD_ID;
        public static final Column.Fk<Ingredient, Long, Food> INGREDIENT_FOOD_ID;
        public static final Column<Ingredient, Double> QUANTITY;
        public static final Column.Fk<Ingredient, String, QtyUnit> QUANTITY_UNIT;
        public static final Column.Fk<Ingredient, Long, Serving> SERVING_ID;
        public static final Column<Ingredient, String> NOTES;

        static {
            ID                 = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME        = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME        = modifyTimeColumnBuildAndAdd(COLUMNS);
            NOTES              = builder("notes", Types.TEXT).buildAndAdd(COLUMNS);
            COMPOSITE_FOOD_ID  = builder("composite_food_id", Types.ID).notEditable().notNull()
                                    .defaultsTo(NO_ID).buildAndAddFk(FoodTable.ID, FoodTable.instance(), COLUMNS);
            INGREDIENT_FOOD_ID = builder("ingredient_food_id", Types.ID).notEditable().notNull()
                                    .defaultsTo(NO_ID).buildAndAddFk(FoodTable.ID, FoodTable.instance(), COLUMNS);
            QUANTITY           = builder("quantity", Types.REAL).notEditable().notNull().buildAndAdd(COLUMNS);
            QUANTITY_UNIT      = builder("quantity_unit", Types.TEXT).notEditable().notNull()
                                    .buildAndAddFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance(), COLUMNS);
            SERVING_ID         = builder("serving_id", Types.ID) // nullable
                                    .buildAndAddFk(ServingTable.ID, ServingTable.instance(), COLUMNS);
        }
        private IngredientTable() {
            super(TABLE_NAME, Ingredient.factory(), COLUMNS);
        }

        private static final IngredientTable INSTANCE = new IngredientTable();

        public static IngredientTable instance() {
            return INSTANCE;
        }
    }

    public final static class RegularMealTable extends BaseTable<RegularMeal> {
        private static final String TABLE_NAME = "RegularMeal";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<RegularMeal, ?>> COLUMNS = new ArrayList<>();

        public static final Column<RegularMeal, Long> ID;
        public static final Column<RegularMeal, Long> CREATE_TIME;
        public static final Column<RegularMeal, Long> MODIFY_TIME;
        public static final Column<RegularMeal, String> NAME;
        public static final Column.Fk<RegularMeal, Long, Meal> MEAL_ID;

        static {
            ID          = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS);
            NAME        = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS);
            MEAL_ID     = builder("meal_id", Types.ID).notEditable().notNull().inSecondaryKey().unique()
                            .buildAndAddFk(MealTable.ID, MealTable.instance(), COLUMNS);
        }

        // declaration has to be last (static initialisation order)
        private static final RegularMealTable INSTANCE = new RegularMealTable();

        public static RegularMealTable instance() {
            return INSTANCE;
        }

        RegularMealTable()  {
            super(TABLE_NAME, RegularMeal.factory(), COLUMNS);
        }
    }

    public final static class NutritionDataTable extends BaseTable<NutritionData> {
        private static final String TABLE_NAME = "NutritionData";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<NutritionData, ?>> COLUMNS = new ArrayList<>();
        /*
         * When adding new columns here (or to any other table), remember to put
         *  - the field declaration (obviously)
         *  - the static initialiser using the builder (won't compile if you don't do this)
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

        static {
            ID                   = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME          = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME          = modifyTimeColumnBuildAndAdd(COLUMNS);
            // FOOD_ID can be null for computed instances
            FOOD_ID              = builder("food_id", Types.ID).notEditable().defaultsTo(NO_ID).inSecondaryKey().unique()
                                     .buildAndAddFk(FoodTable.ID, FoodTable.instance(), COLUMNS);
            QUANTITY             = builder("quantity", Types.REAL).notNull().defaultsTo(100.0).buildAndAdd(COLUMNS);
            QUANTITY_UNIT        = builder("quantity_unit", Types.TEXT).notNull()
                                     .buildAndAddFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance(), COLUMNS);
            DATA_SOURCE          = builder("data_source", Types.TEXT).buildAndAdd(COLUMNS);
            DENSITY              = builder("density", Types.REAL).buildAndAdd(COLUMNS);
            KILOJOULES           = builder("kilojoules", Types.REAL).buildAndAdd(COLUMNS);
            CALORIES             = builder("calories", Types.REAL).buildAndAdd(COLUMNS);
            PROTEIN              = builder("protein", Types.REAL).buildAndAdd(COLUMNS);
            FAT                  = builder("fat", Types.REAL).buildAndAdd(COLUMNS);
            SATURATED_FAT        = builder("saturated_fat", Types.REAL).buildAndAdd(COLUMNS);
            CARBOHYDRATE         = builder("carbohydrate", Types.REAL).buildAndAdd(COLUMNS);
            SUGAR                = builder("sugar", Types.REAL).buildAndAdd(COLUMNS);
            FIBRE                = builder("fibre", Types.REAL).buildAndAdd(COLUMNS);
            SODIUM               = builder("sodium", Types.REAL).buildAndAdd(COLUMNS);
            POTASSIUM            = builder("potassium", Types.REAL).buildAndAdd(COLUMNS);
            CALCIUM              = builder("calcium", Types.REAL).buildAndAdd(COLUMNS);
            IRON                 = builder("iron", Types.REAL).buildAndAdd(COLUMNS);
            MONOUNSATURATED_FAT  = builder("monounsaturated_fat", Types.REAL).buildAndAdd(COLUMNS);
            POLYUNSATURATED_FAT  = builder("polyunsaturated_fat", Types.REAL).buildAndAdd(COLUMNS);
            OMEGA_3_FAT          = builder("omega_3", Types.REAL).buildAndAdd(COLUMNS);
            OMEGA_6_FAT          = builder("omega_6", Types.REAL).buildAndAdd(COLUMNS);
            STARCH               = builder("starch", Types.REAL).buildAndAdd(COLUMNS);
            SALT                 = builder("salt", Types.REAL).buildAndAdd(COLUMNS);
            WATER                = builder("water", Types.REAL).buildAndAdd(COLUMNS);
            CARBOHYDRATE_BY_DIFF = builder("carbohydrate_by_diff", Types.REAL).buildAndAdd(COLUMNS);
            ALCOHOL              = builder("alcohol", Types.REAL).buildAndAdd(COLUMNS);
            SUGAR_ALCOHOL        = builder("sugar_alcohol", Types.REAL).buildAndAdd(COLUMNS);
        }

        // this part has to be last (static initialisation order)
        private static final NutritionDataTable INSTANCE = new NutritionDataTable();

        public static NutritionDataTable instance() {
            return INSTANCE;
        }

        private NutritionDataTable() {
            super(TABLE_NAME, NutritionData.factory(), COLUMNS);
        }

    }

    public final static class FoodAttributeTable extends BaseTable<FoodAttribute> {
        private static final String TABLE_NAME = "FoodAttribute";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<FoodAttribute, ?>> COLUMNS = new ArrayList<>();

        public static final Column<FoodAttribute, Long> ID;
        public static final Column<FoodAttribute, Long> CREATE_TIME;
        public static final Column<FoodAttribute, Long> MODIFY_TIME;
        public static final Column<FoodAttribute, String> NAME;

        static {
            ID          = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS);
            NAME        = builder("name", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS);
        }

        // this declaration has to be last (static initialisation order)
        private static final FoodAttributeTable INSTANCE = new FoodAttributeTable();

        public static FoodAttributeTable instance() {
            return INSTANCE;
        }

        private FoodAttributeTable() {
            super(TABLE_NAME, FoodAttribute.factory(), COLUMNS);
        }
    }

    public final static class AttrMappingTable extends BaseTable<AttrMapping> {
        private static final String TABLE_NAME = "AttributeMapping";
        // holds the following columns in the order initialised in the static block
        private static final List<Column<AttrMapping, ?>> COLUMNS = new ArrayList<>();

        public static final Column<AttrMapping, Long> ID;
        public static final Column<AttrMapping, Long> CREATE_TIME;
        public static final Column<AttrMapping, Long> MODIFY_TIME;
        public static final Column.Fk<AttrMapping, Long, Food> FOOD_ID;
        public static final Column.Fk<AttrMapping, Long, FoodAttribute> ATTRIBUTE_ID;


        static {
            ID           = idColumnBuildAndAdd(COLUMNS);
            CREATE_TIME  = createTimeColumnBuildAndAdd(COLUMNS);
            MODIFY_TIME  = modifyTimeColumnBuildAndAdd(COLUMNS);
            FOOD_ID      = builder("food_id", Types.ID).notEditable().notNull().inSecondaryKey()
                            .buildAndAddFk(FoodTable.ID, FoodTable.instance(), COLUMNS);
            ATTRIBUTE_ID = builder("attribute_id", Types.ID).notEditable().notNull().inSecondaryKey()
                            .buildAndAddFk(FoodAttributeTable.ID, FoodAttributeTable.instance(), COLUMNS);
        }

        // this declaration has to be last (static initialisation order)
        private static final AttrMappingTable INSTANCE = new AttrMappingTable();

        public static AttrMappingTable instance() {
            return INSTANCE;
        }

        private AttrMappingTable() {
            super(TABLE_NAME, AttrMapping.factory(), COLUMNS);
        }
    }
}

// @formatter:on
