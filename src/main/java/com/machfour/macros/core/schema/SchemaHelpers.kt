package com.machfour.macros.core.schema

import com.machfour.macros.core.Column
import com.machfour.macros.core.ColumnImpl
import com.machfour.macros.core.datatype.MacrosType
import com.machfour.macros.core.datatype.Types

import com.machfour.macros.core.MacrosEntity.Companion.NO_ID

/**
 * The classes in this package hold the database column information for all entities used in this library.
 * You will know very quickly if you break anything in this class, because basically everything uses
 * the tables and columns defined here.
 */
internal object SchemaHelpers {

    internal const val ID_COLUMN_NAME = "id"
    internal const val CREATE_TIME_COLUMN_NAME = "create_time"
    internal const val MODIFY_TIME_COLUMN_NAME = "modify_time"
    internal const val NOTES_COLUMN_NAME = "notes"

    internal fun <J: Any> builder(name: String, type: MacrosType<J>): ColumnImpl.Builder<J> {
        return ColumnImpl.Builder(name, type)
    }

    internal fun <M> idColumnBuildAndAdd(columnList: MutableList<Column<M, *>>)
        = builder(ID_COLUMN_NAME, Types.ID).defaultsTo(NO_ID).notNull().unique().notEditable().buildAndAdd(columnList)

    internal fun <M> createTimeColumnBuildAndAdd(columnList: MutableList<Column<M, *>>)
        = builder(CREATE_TIME_COLUMN_NAME, Types.TIMESTAMP).defaultsTo(0L).notEditable().buildAndAdd(columnList)

    internal fun <M> modifyTimeColumnBuildAndAdd(columnList: MutableList<Column<M, *>>)
        = builder(MODIFY_TIME_COLUMN_NAME, Types.TIMESTAMP).defaultsTo(0L).notEditable().buildAndAdd(columnList)

    internal fun <M> notesColumnBuildAndAdd(columnList: MutableList<Column<M, *>>)
        = builder(NOTES_COLUMN_NAME, Types.TEXT).buildAndAdd(columnList)

    // shared columns for FoodQuantity tables (FoodPortion, Ingredient)

    internal fun <M> foodQuantityQuantityCol(columns: MutableList<Column<M, *>>)
        = builder("quantity", Types.REAL).notNull().buildAndAdd(columns)

    internal fun <M> foodQuantityQuantityUnitCol(columns: MutableList<Column<M, *>>)
        = builder("quantity_unit", Types.TEXT).notEditable().notNull()
            .buildAndAddFk(UnitTable.ABBREVIATION, UnitTable.instance, columns)

    internal fun <M> foodQuantityFoodIdCol(columns: MutableList<Column<M, *>>)
        = builder("food_id", Types.ID).notEditable().notNull()
            .buildAndAddFk(FoodTable.ID, FoodTable.instance, columns)

    internal fun <M> foodQuantityServingIdCol(columns: MutableList<Column<M, *>>)
        = builder("serving_id", Types.ID).notEditable()
            .buildAndAddFk(ServingTable.ID, ServingTable.instance, columns)

    internal fun <M> foodQuantityNutrientMaxVersionCol(columns: MutableList<Column<M, *>>)
        = builder("nutrient_max_version", Types.INTEGER).notEditable().notNull().defaultsTo(1)
            .buildAndAdd(columns)

    // Shared columns for NutrientValue tables (FoodNutrientValue, MealNutrientGoalValue, DayNutrientGoalValue)

    internal fun <M> nutrientValueNutrientColumn(columns: MutableList<Column<M, *>>)
        = builder("nutrient_id", Types.ID).notNull().notEditable().inSecondaryKey()
            .buildAndAddFk(NutrientTable.ID, NutrientTable.instance, columns)
    
    internal fun <M> nutrientValueUnitColumn(columns: MutableList<Column<M, *>>)
        = builder("unit_id", Types.ID).notNull().notEditable()
                .buildAndAddFk(UnitTable.ID, UnitTable.instance, columns)

    internal fun <M> nutrientValueValueColumn(columns: MutableList<Column<M, *>>)
        = builder("value", Types.REAL).notNull().buildAndAdd(columns)

    internal fun <M> nutrientValueConstraintColumn(columns: MutableList<Column<M, *>>)
        = builder("constraint_spec", Types.INTEGER).notNull().defaultsTo(0).buildAndAdd(columns)

}

