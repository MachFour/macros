package com.machfour.macros.core

import com.machfour.macros.core.datatype.MacrosType
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.objects.*
import com.machfour.macros.util.DateStamp

import java.util.ArrayList

import com.machfour.macros.core.MacrosEntity.Companion.NO_ID


// @formatter:off

/**
 * Class holding the database column information for all persistable entities used in this library
 * You will know very quickly if you break anything in this class, because basically everything uses
 * the tables and columns defined here.
 */
object Schema {

    internal const val ID_COLUMN_NAME = "id"
    internal const val CREATE_TIME_COLUMN_NAME = "create_time"
    internal const val MODIFY_TIME_COLUMN_NAME = "modify_time"


    private fun <M> idColumnBuildAndAdd(columnList: MutableList<Column<M, *>>): Column<M, Long> {
        return builder(ID_COLUMN_NAME, Types.ID).defaultsTo(NO_ID).notNull().unique().notEditable()
                .buildAndAdd(columnList)
    }

    private fun <M> createTimeColumnBuildAndAdd(columnList: MutableList<Column<M, *>>): Column<M, Long> {
        return builder(CREATE_TIME_COLUMN_NAME, Types.TIMESTAMP).defaultsTo(0L).notEditable().buildAndAdd(columnList)
    }

    private fun <M> modifyTimeColumnBuildAndAdd(columnList: MutableList<Column<M, *>>): Column<M, Long> {
        return builder(MODIFY_TIME_COLUMN_NAME, Types.TIMESTAMP).defaultsTo(0L).notEditable()
                .buildAndAdd(columnList)
    }

    private fun <J> builder(name: String, type: MacrosType<J>): ColumnImpl.Builder<J> {
        return ColumnImpl.Builder(name, type)
    }

    // QtyUnit.factory() causes initialisation of QtyUnit, which depends on this class.
    // So the columns are initialised as a side effect of calling this function.
    class QtyUnitTable private constructor()
        : BaseTable<QtyUnit>(TABLE_NAME, QtyUnit.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "QtyUnit"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<QtyUnit, *>>()

            // initialisation order here is the order of iteration of the columns
            val ID: Column<QtyUnit, Long>
            val CREATE_TIME: Column<QtyUnit, Long>
            val MODIFY_TIME: Column<QtyUnit, Long>
            val NAME: Column<QtyUnit, String>
            val ABBREVIATION: Column<QtyUnit, String>
            val IS_VOLUME_UNIT: Column<QtyUnit, Boolean>
            val METRIC_EQUIVALENT: Column<QtyUnit, Double>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
                ABBREVIATION = builder("abbreviation", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS)
                IS_VOLUME_UNIT = builder("is_volume_unit", Types.BOOLEAN).notNull().buildAndAdd(COLUMNS)
                METRIC_EQUIVALENT = builder("metric_equivalent", Types.REAL).notNull().buildAndAdd(COLUMNS)
            }

            // this declaration has to come last (static initialisation order
            val instance = QtyUnitTable()
        }
    }

    class FoodTable private constructor() : BaseTable<Food>(TABLE_NAME, Food.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "Food"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<Food, *>>()

            val ID: Column<Food, Long>
            val CREATE_TIME: Column<Food, Long>
            val MODIFY_TIME: Column<Food, Long>
            val INDEX_NAME: Column<Food, String>
            val BRAND: Column<Food, String>
            val VARIETY: Column<Food, String>
            val VARIETY_AFTER_NAME: Column<Food, Boolean>
            val NAME: Column<Food, String>
            val NOTES: Column<Food, String>
            val FOOD_TYPE: Column<Food, String>
            val USDA_INDEX: Column<Food, Long>
            val NUTTAB_INDEX: Column<Food, String>
            val CATEGORY: Column.Fk<Food, String, FoodCategory>

            init {
                // order of initialisation is order that columns will be iterated through
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                INDEX_NAME = builder("index_name", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
                BRAND = builder("brand", Types.TEXT).buildAndAdd(COLUMNS)
                VARIETY = builder("variety", Types.TEXT).buildAndAdd(COLUMNS)
                VARIETY_AFTER_NAME = builder("variety_after_name", Types.BOOLEAN).notNull()
                        .defaultsTo(false).buildAndAdd(COLUMNS)
                NOTES = builder("notes", Types.TEXT).buildAndAdd(COLUMNS)
                CATEGORY = builder("category", Types.TEXT).notNull()
                        .buildAndAddFk(FoodCategoryTable.NAME, FoodCategoryTable.instance, COLUMNS)
                FOOD_TYPE = builder("food_type", Types.TEXT).notEditable().notNull()
                        .defaultsTo(FoodType.PRIMARY.niceName).buildAndAdd(COLUMNS)
                USDA_INDEX = builder("usda_index", Types.INTEGER).notEditable().buildAndAdd(COLUMNS)
                NUTTAB_INDEX = builder("nuttab_index", Types.TEXT).notEditable().buildAndAdd(COLUMNS)
            }

            // this has to come last (static initialisation order)
            val instance = FoodTable()
        }

    }

    class ServingTable private constructor() : BaseTable<Serving>(TABLE_NAME, Serving.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "Serving"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<Serving, *>>()

            val ID: Column<Serving, Long>
            val CREATE_TIME: Column<Serving, Long>
            val MODIFY_TIME: Column<Serving, Long>
            val NAME: Column<Serving, String>
            val QUANTITY: Column<Serving, Double>
            val IS_DEFAULT: Column<Serving, Boolean>
            val FOOD_ID: Column.Fk<Serving, Long, Food>
            val QUANTITY_UNIT: Column.Fk<Serving, String, QtyUnit>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
                QUANTITY = builder("quantity", Types.REAL).notNull().buildAndAdd(COLUMNS)
                QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notNull()
                        .buildAndAddFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance, COLUMNS)
                IS_DEFAULT = builder("is_default", Types.NULLBOOLEAN).notNull().defaultsTo(false).buildAndAdd(COLUMNS)
                FOOD_ID = builder("food_id", Types.ID).notEditable().notNull().defaultsTo(NO_ID)
                        .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
            }

            // this declaration has to be last (static initialisation order)
            val instance = ServingTable()
        }
    }

    class FoodPortionTable private constructor() : BaseTable<FoodPortion>(TABLE_NAME, FoodPortion.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "FoodPortion"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<FoodPortion, *>>()

            val ID: Column<FoodPortion, Long>
            val CREATE_TIME: Column<FoodPortion, Long>
            val MODIFY_TIME: Column<FoodPortion, Long>
            val QUANTITY: Column<FoodPortion, Double>
            val QUANTITY_UNIT: Column.Fk<FoodPortion, String, QtyUnit>
            val FOOD_ID: Column.Fk<FoodPortion, Long, Food>
            val MEAL_ID: Column.Fk<FoodPortion, Long, Meal>
            val SERVING_ID: Column.Fk<FoodPortion, Long, Serving>
            val NOTES: Column<FoodPortion, String>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                QUANTITY = builder("quantity", Types.REAL).notNull().buildAndAdd(COLUMNS)
                QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notEditable().notNull()
                        .buildAndAddFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance, COLUMNS)
                FOOD_ID = builder("food_id", Types.ID).notEditable().notNull()
                        .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
                MEAL_ID = builder("meal_id", Types.ID).notEditable().notNull()
                        .buildAndAddFk(MealTable.ID, MealTable.instance, COLUMNS)
                SERVING_ID = builder("serving_id", Types.ID).notEditable()
                        .buildAndAddFk(ServingTable.ID, ServingTable.instance, COLUMNS)
                NOTES = builder("notes", Types.TEXT).buildAndAdd(COLUMNS)
            }


            val instance = FoodPortionTable()
        }
    }

    class MealTable private constructor() : BaseTable<Meal>(TABLE_NAME, Meal.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "Meal"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<Meal, *>>()

            val ID: Column<Meal, Long>
            val CREATE_TIME: Column<Meal, Long>
            val MODIFY_TIME: Column<Meal, Long>
            val NAME: Column<Meal, String>
            val DAY: Column<Meal, DateStamp>
            val START_TIME: Column<Meal, Long>
            val DURATION: Column<Meal, Long>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
                DAY = builder("day", Types.DATESTAMP).notNull().buildAndAdd(COLUMNS)
                START_TIME = builder("start_time", Types.TIMESTAMP).notNull().defaultsTo(0L).buildAndAdd(COLUMNS)
                DURATION = builder("duration", Types.INTEGER).notNull().defaultsTo(0L).buildAndAdd(COLUMNS)
            }

            val instance = MealTable()
        }
    }

    class FoodCategoryTable private constructor() : BaseTable<FoodCategory>(TABLE_NAME, FoodCategory.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "FoodCategory"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<FoodCategory, *>>()

            val ID: Column<FoodCategory, Long>
            val CREATE_TIME: Column<FoodCategory, Long>
            val MODIFY_TIME: Column<FoodCategory, Long>
            val NAME: Column<FoodCategory, String>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS)
            }

            val instance = FoodCategoryTable()
        }

    }

    class IngredientTable private constructor() : BaseTable<Ingredient>(TABLE_NAME, Ingredient.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "Ingredient"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<Ingredient, *>>()

            val ID: Column<Ingredient, Long>
            val CREATE_TIME: Column<Ingredient, Long>
            val MODIFY_TIME: Column<Ingredient, Long>
            val COMPOSITE_FOOD_ID: Column.Fk<Ingredient, Long, Food>
            val INGREDIENT_FOOD_ID: Column.Fk<Ingredient, Long, Food>
            val QUANTITY: Column<Ingredient, Double>
            val QUANTITY_UNIT: Column.Fk<Ingredient, String, QtyUnit>
            val SERVING_ID: Column.Fk<Ingredient, Long, Serving>
            val NOTES: Column<Ingredient, String>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NOTES = builder("notes", Types.TEXT).buildAndAdd(COLUMNS)
                COMPOSITE_FOOD_ID = builder("composite_food_id", Types.ID).notEditable().notNull()
                        .defaultsTo(NO_ID).buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
                INGREDIENT_FOOD_ID = builder("ingredient_food_id", Types.ID).notEditable().notNull()
                        .defaultsTo(NO_ID).buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
                QUANTITY = builder("quantity", Types.REAL).notEditable().notNull().buildAndAdd(COLUMNS)
                QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notEditable().notNull()
                        .buildAndAddFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance, COLUMNS)
                SERVING_ID = builder("serving_id", Types.ID) // nullable
                        .buildAndAddFk(ServingTable.ID, ServingTable.instance, COLUMNS)
            }

            val instance = IngredientTable()
        }
    }

    class RegularMealTable private constructor() : BaseTable<RegularMeal>(TABLE_NAME, RegularMeal.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "RegularMeal"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<RegularMeal, *>>()

            val ID: Column<RegularMeal, Long>
            val CREATE_TIME: Column<RegularMeal, Long>
            val MODIFY_TIME: Column<RegularMeal, Long>
            val NAME: Column<RegularMeal, String>
            val MEAL_ID: Column.Fk<RegularMeal, Long, Meal>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
                MEAL_ID = builder("meal_id", Types.ID).notEditable().notNull().inSecondaryKey().unique()
                        .buildAndAddFk(MealTable.ID, MealTable.instance, COLUMNS)
            }

            // declaration has to be last (static initialisation order)
            val instance = RegularMealTable()
        }
    }

    class NutritionDataTable private constructor() : BaseTable<NutritionData>(TABLE_NAME, NutritionData.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "NutritionData"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<NutritionData, *>>()

            /*
             * When adding new columns here (or to any other table), remember to put
             *  - the field declaration (obviously)
             *  - the static initialiser using the builder (won't compile if you don't do this)
             * And additionally, for NutritionData, add to NutritionData.NUTRIENT_COLUMNS if appropriate
            */
            val ID: Column<NutritionData, Long>
            val CREATE_TIME: Column<NutritionData, Long>
            val MODIFY_TIME: Column<NutritionData, Long>
            val DATA_SOURCE: Column<NutritionData, String>
            val QUANTITY: Column<NutritionData, Double>
            val DENSITY: Column<NutritionData, Double>
            val KILOJOULES: Column<NutritionData, Double>
            val CALORIES: Column<NutritionData, Double>
            val PROTEIN: Column<NutritionData, Double>
            val CARBOHYDRATE: Column<NutritionData, Double>
            val CARBOHYDRATE_BY_DIFF: Column<NutritionData, Double>
            val SUGAR: Column<NutritionData, Double>
            val SUGAR_ALCOHOL: Column<NutritionData, Double>
            val STARCH: Column<NutritionData, Double>
            val FAT: Column<NutritionData, Double>
            val SATURATED_FAT: Column<NutritionData, Double>
            val MONOUNSATURATED_FAT: Column<NutritionData, Double>
            val POLYUNSATURATED_FAT: Column<NutritionData, Double>
            val OMEGA_3_FAT: Column<NutritionData, Double>
            val OMEGA_6_FAT: Column<NutritionData, Double>
            val FIBRE: Column<NutritionData, Double>
            val SODIUM: Column<NutritionData, Double>
            val SALT: Column<NutritionData, Double>
            val POTASSIUM: Column<NutritionData, Double>
            val CALCIUM: Column<NutritionData, Double>
            val IRON: Column<NutritionData, Double>
            val WATER: Column<NutritionData, Double>
            val ALCOHOL: Column<NutritionData, Double>
            val FOOD_ID: Column.Fk<NutritionData, Long, Food>
            val QUANTITY_UNIT: Column.Fk<NutritionData, String, QtyUnit>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                // FOOD_ID can be null for computed instances
                FOOD_ID = builder("food_id", Types.ID).notEditable().defaultsTo(NO_ID).inSecondaryKey().unique()
                        .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
                QUANTITY = builder("quantity", Types.REAL).notNull().defaultsTo(100.0).buildAndAdd(COLUMNS)
                QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notNull()
                        .buildAndAddFk(QtyUnitTable.ABBREVIATION, QtyUnitTable.instance, COLUMNS)
                DATA_SOURCE = builder("data_source", Types.TEXT).buildAndAdd(COLUMNS)
                DENSITY = builder("density", Types.REAL).buildAndAdd(COLUMNS)
                KILOJOULES = builder("kilojoules", Types.REAL).buildAndAdd(COLUMNS)
                CALORIES = builder("calories", Types.REAL).buildAndAdd(COLUMNS)
                PROTEIN = builder("protein", Types.REAL).buildAndAdd(COLUMNS)
                FAT = builder("fat", Types.REAL).buildAndAdd(COLUMNS)
                SATURATED_FAT = builder("saturated_fat", Types.REAL).buildAndAdd(COLUMNS)
                CARBOHYDRATE = builder("carbohydrate", Types.REAL).buildAndAdd(COLUMNS)
                SUGAR = builder("sugar", Types.REAL).buildAndAdd(COLUMNS)
                FIBRE = builder("fibre", Types.REAL).buildAndAdd(COLUMNS)
                SODIUM = builder("sodium", Types.REAL).buildAndAdd(COLUMNS)
                POTASSIUM = builder("potassium", Types.REAL).buildAndAdd(COLUMNS)
                CALCIUM = builder("calcium", Types.REAL).buildAndAdd(COLUMNS)
                IRON = builder("iron", Types.REAL).buildAndAdd(COLUMNS)
                MONOUNSATURATED_FAT = builder("monounsaturated_fat", Types.REAL).buildAndAdd(COLUMNS)
                POLYUNSATURATED_FAT = builder("polyunsaturated_fat", Types.REAL).buildAndAdd(COLUMNS)
                OMEGA_3_FAT = builder("omega_3", Types.REAL).buildAndAdd(COLUMNS)
                OMEGA_6_FAT = builder("omega_6", Types.REAL).buildAndAdd(COLUMNS)
                STARCH = builder("starch", Types.REAL).buildAndAdd(COLUMNS)
                SALT = builder("salt", Types.REAL).buildAndAdd(COLUMNS)
                WATER = builder("water", Types.REAL).buildAndAdd(COLUMNS)
                CARBOHYDRATE_BY_DIFF = builder("carbohydrate_by_diff", Types.REAL).buildAndAdd(COLUMNS)
                ALCOHOL = builder("alcohol", Types.REAL).buildAndAdd(COLUMNS)
                SUGAR_ALCOHOL = builder("sugar_alcohol", Types.REAL).buildAndAdd(COLUMNS)
            }

            // this part has to be last (static initialisation order)
            val instance = NutritionDataTable()
        }

    }

    class FoodAttributeTable private constructor() : BaseTable<FoodAttribute>(TABLE_NAME, FoodAttribute.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "FoodAttribute"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<FoodAttribute, *>>()

            val ID: Column<FoodAttribute, Long>
            val CREATE_TIME: Column<FoodAttribute, Long>
            val MODIFY_TIME: Column<FoodAttribute, Long>
            val NAME: Column<FoodAttribute, String>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS)
            }

            // this declaration has to be last (static initialisation order)
            val instance = FoodAttributeTable()
        }
    }

    class AttrMappingTable private constructor() : BaseTable<AttrMapping>(TABLE_NAME, AttrMapping.factory(), COLUMNS) {
        companion object {
            private const val TABLE_NAME = "AttributeMapping"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<AttrMapping, *>>()

            val ID: Column<AttrMapping, Long>
            val CREATE_TIME: Column<AttrMapping, Long>
            val MODIFY_TIME: Column<AttrMapping, Long>
            val FOOD_ID: Column.Fk<AttrMapping, Long, Food>
            val ATTRIBUTE_ID: Column.Fk<AttrMapping, Long, FoodAttribute>


            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                FOOD_ID = builder("food_id", Types.ID).notEditable().notNull().inSecondaryKey()
                        .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
                ATTRIBUTE_ID = builder("attribute_id", Types.ID).notEditable().notNull().inSecondaryKey()
                        .buildAndAddFk(FoodAttributeTable.ID, FoodAttributeTable.instance, COLUMNS)
            }

            // this declaration has to be last (static initialisation order)
            val instance = AttrMappingTable()
        }
    }
}

// @formatter:on
