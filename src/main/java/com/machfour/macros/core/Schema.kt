package com.machfour.macros.core

import com.machfour.macros.core.datatype.MacrosType
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.util.DateStamp

import java.util.ArrayList

import com.machfour.macros.core.MacrosEntity.Companion.NO_ID
import java.time.Instant


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

    private fun <J: Any> builder(name: String, type: MacrosType<J>): ColumnImpl.Builder<J> {
        return ColumnImpl.Builder(name, type)
    }

    // Unit.factory() causes initialisation of Unit, which depends on this class.
    // So the columns are initialised as a side effect of calling that function.
    class UnitTable private constructor()
        : BaseTable<Unit>(TABLE_NAME, Unit.factory, COLUMNS) {
        companion object {
            private const val TABLE_NAME = "Unit"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<Unit, *>>()

            // initialisation order here is the order of iteration of the columns
            val ID: Column<Unit, Long>
            val CREATE_TIME: Column<Unit, Long>
            val MODIFY_TIME: Column<Unit, Long>
            val TYPE_ID: Column<Unit, Long>
            val NAME: Column<Unit, String>
            val ABBREVIATION: Column<Unit, String>
            val METRIC_EQUIVALENT: Column<Unit, Double>
            val INBUILT: Column<Unit, Boolean>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
                TYPE_ID = builder("type_id", Types.INTEGER).notEditable().notNull().buildAndAdd(COLUMNS)
                ABBREVIATION = builder("abbreviation", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS)
                METRIC_EQUIVALENT = builder("metric_equivalent", Types.REAL).notNull().buildAndAdd(COLUMNS)
                INBUILT = builder("inbuilt", Types.BOOLEAN).notNull().defaultsTo(false).buildAndAdd(COLUMNS)
            }

            // this declaration has to come last (static initialisation order
            val instance = UnitTable()
        }
    }

    class FoodTable private constructor() : BaseTable<Food>(TABLE_NAME, Food.factory, COLUMNS) {
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
            val EXTRA_DESC: Column<Food, String>
            val NAME: Column<Food, String>
            val NOTES: Column<Food, String>
            val FOOD_TYPE: Column<Food, String>
            val USDA_INDEX: Column<Food, Long>
            val NUTTAB_INDEX: Column<Food, String>
            val DATA_SOURCE: Column<Food, String>
            val DATA_NOTES: Column<Food, String>
            val DENSITY: Column<Food, Double>
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
                EXTRA_DESC = builder("extra_desc", Types.TEXT).buildAndAdd(COLUMNS)
                NOTES = builder("notes", Types.TEXT).buildAndAdd(COLUMNS)
                CATEGORY = builder("category", Types.TEXT).notNull()
                        .buildAndAddFk(FoodCategoryTable.NAME, FoodCategoryTable.instance, COLUMNS)
                FOOD_TYPE = builder("food_type", Types.TEXT).notEditable().notNull()
                        .defaultsTo(FoodType.PRIMARY.niceName).buildAndAdd(COLUMNS)
                USDA_INDEX = builder("usda_index", Types.INTEGER).notEditable().buildAndAdd(COLUMNS)
                NUTTAB_INDEX = builder("nuttab_index", Types.TEXT).notEditable().buildAndAdd(COLUMNS)
                DATA_SOURCE = builder("data_source", Types.TEXT).buildAndAdd(COLUMNS)
                DATA_NOTES = builder("data_notes", Types.TEXT).buildAndAdd(COLUMNS)
                DENSITY = builder("density", Types.REAL).buildAndAdd(COLUMNS)
            }

            // this has to come last (static initialisation order)
            val instance = FoodTable()
        }

    }

    class ServingTable private constructor() : BaseTable<Serving>(TABLE_NAME, Serving.factory, COLUMNS) {
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
            val QUANTITY_UNIT: Column.Fk<Serving, String, Unit>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
                QUANTITY = builder("quantity", Types.REAL).notNull().buildAndAdd(COLUMNS)
                QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notNull()
                        .buildAndAddFk(UnitTable.ABBREVIATION, UnitTable.instance, COLUMNS)
                IS_DEFAULT = builder("is_default", Types.NULLBOOLEAN).notNull().defaultsTo(false).buildAndAdd(COLUMNS)
                FOOD_ID = builder("food_id", Types.ID).notEditable().notNull().defaultsTo(NO_ID)
                        .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
            }

            // this declaration has to be last (static initialisation order)
            val instance = ServingTable()
        }
    }

    class MealTable private constructor() : BaseTable<Meal>(TABLE_NAME, Meal.factory, COLUMNS) {
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
            val NOTES: Column<Meal, String>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
                DAY = builder("day", Types.DATESTAMP).notNull().default { DateStamp.currentDate() }.buildAndAdd(COLUMNS)
                START_TIME = builder("start_time", Types.TIMESTAMP).notNull().default{ Instant.now().epochSecond }.buildAndAdd(COLUMNS)
                DURATION = builder("duration", Types.INTEGER).notNull().defaultsTo(0L).buildAndAdd(COLUMNS)
                NOTES = builder("notes", Types.TEXT).buildAndAdd(COLUMNS)
            }

            val instance = MealTable()
        }
    }


    // needs to come after FoodTable, ServingTable, MealTable
    class FoodQuantityTable private constructor() : BaseTable<FoodQuantity>(TABLE_NAME, FoodQuantity.factory, COLUMNS) {
        companion object {
            private const val TABLE_NAME = "FoodQuantity"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<FoodQuantity, *>>()

            val ID: Column<FoodQuantity, Long>
            val CREATE_TIME: Column<FoodQuantity, Long>
            val MODIFY_TIME: Column<FoodQuantity, Long>
            val QUANTITY: Column<FoodQuantity, Double>
            val QUANTITY_UNIT: Column.Fk<FoodQuantity, String, Unit>
            val FOOD_ID: Column.Fk<FoodQuantity, Long, Food>
            val MEAL_ID: Column.Fk<FoodQuantity, Long, Meal>
            val PARENT_FOOD_ID: Column.Fk<FoodQuantity, Long, Food>
            val SERVING_ID: Column.Fk<FoodQuantity, Long, Serving>
            val NOTES: Column<FoodQuantity, String>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                QUANTITY = builder("quantity", Types.REAL).notNull().buildAndAdd(COLUMNS)
                QUANTITY_UNIT = builder("quantity_unit", Types.TEXT).notEditable().notNull()
                        .buildAndAddFk(UnitTable.ABBREVIATION, UnitTable.instance, COLUMNS)
                FOOD_ID = builder("food_id", Types.ID).notEditable().notNull()
                        .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
                MEAL_ID = builder("meal_id", Types.ID).notEditable()
                        .buildAndAddFk(MealTable.ID, MealTable.instance, COLUMNS)
                PARENT_FOOD_ID = builder("parent_food_id", Types.ID).notEditable()
                        .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
                SERVING_ID = builder("serving_id", Types.ID).notEditable()
                        .buildAndAddFk(ServingTable.ID, ServingTable.instance, COLUMNS)
                NOTES = builder("notes", Types.TEXT).buildAndAdd(COLUMNS)
            }


            val instance = FoodQuantityTable()
        }
    }

    class FoodCategoryTable private constructor() : BaseTable<FoodCategory>(TABLE_NAME, FoodCategory.factory, COLUMNS) {
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

    class RegularMealTable private constructor() : BaseTable<RegularMeal>(TABLE_NAME, RegularMeal.factory, COLUMNS) {
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

    class NutrientTable private constructor() : BaseTable<Nutrient>(TABLE_NAME, Nutrient.factory, COLUMNS) {
        companion object {
            private const val TABLE_NAME = "Nutrient"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<Nutrient, *>>()

            val ID: Column<Nutrient, Long>
            val CREATE_TIME: Column<Nutrient, Long>
            val MODIFY_TIME: Column<Nutrient, Long>
            val NAME: Column<Nutrient, String>
            val UNIT_TYPES: Column<Nutrient, Long>
            val INBUILT: Column<Nutrient, Boolean>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NAME = builder("name", Types.TEXT).notNull().unique().inSecondaryKey().buildAndAdd(COLUMNS)
                UNIT_TYPES = builder("unit_types", Types.INTEGER).notNull().buildAndAdd(COLUMNS)
                INBUILT = builder("inbuilt", Types.BOOLEAN).notNull().defaultsTo(false).buildAndAdd(COLUMNS)
            }

            // this part has to be last (static initialisation order)
            val instance = NutrientTable()
        }

    }

    class NutrientValueTable private constructor() : BaseTable<NutrientValue>(TABLE_NAME, NutrientValue.factory, COLUMNS) {
        companion object {
            private const val TABLE_NAME = "NutrientValue"

            // holds the following columns in the order initialised in the static block
            private val COLUMNS = ArrayList<Column<NutrientValue, *>>()

            val ID: Column<NutrientValue, Long>
            val CREATE_TIME: Column<NutrientValue, Long>
            val MODIFY_TIME: Column<NutrientValue, Long>
            val NUTRIENT_ID: Column.Fk<NutrientValue, Long, Nutrient>
            val FOOD_ID: Column.Fk<NutrientValue, Long, Food>
            val VALUE: Column<NutrientValue, Double>
            val UNIT_ID: Column.Fk<NutrientValue, Long, Unit>

            init {
                ID = idColumnBuildAndAdd(COLUMNS)
                CREATE_TIME = createTimeColumnBuildAndAdd(COLUMNS)
                MODIFY_TIME = modifyTimeColumnBuildAndAdd(COLUMNS)
                NUTRIENT_ID = builder("nutrient_id", Types.ID).notNull().notEditable().inSecondaryKey()
                        .buildAndAddFk(NutrientTable.ID, NutrientTable.instance, COLUMNS)
                FOOD_ID = builder("food_id", Types.ID).notNull().notEditable().defaultsTo(NO_ID).inSecondaryKey()
                        .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
                VALUE = builder("value", Types.REAL).notNull().buildAndAdd(COLUMNS)
                UNIT_ID = builder("unit_id", Types.ID).notNull().notEditable()
                        .buildAndAddFk(UnitTable.ID, UnitTable.instance, COLUMNS)
            }

            // this part has to be last (static initialisation order)
            val instance = NutrientValueTable()
        }

    }

    class FoodAttributeTable private constructor() : BaseTable<FoodAttribute>(TABLE_NAME, FoodAttribute.factory, COLUMNS) {
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

    class AttrMappingTable private constructor() : BaseTable<AttrMapping>(TABLE_NAME, AttrMapping.factory, COLUMNS) {
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
