package com.machfour.macros.orm.schema

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "FoodNutrientValue"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<FoodNutrientValue, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)

private val _NUTRIENT_ID = nutrientValueNutrientColumn(COLUMNS)
private val _UNIT_ID = nutrientValueUnitColumn(COLUMNS)
private val _VALUE = nutrientValueValueColumn(COLUMNS)
private val _CONSTRAINT_SPEC = nutrientValueConstraintColumn(COLUMNS)

// might have NO_ID value if it's not being stored in the database (i.e computed value)
private val _FOOD_ID =
    builder("food_id", Types.ID).notNull().notEditable().defaultsTo(MacrosEntity.NO_ID).inSecondaryKey()
        .buildFkFor(FoodTable, FoodTable.ID, COLUMNS)
private val _VERSION =
    builder("version", Types.INTEGER).notNull().defaultsTo(1).buildFor(COLUMNS)

object FoodNutrientValueTable: TableImpl<FoodNutrientValue>(TABLE_NAME, Factories.foodNutrientValue, COLUMNS) {
    val ID: Column<FoodNutrientValue, Long>
        get() = _ID
    val CREATE_TIME: Column<FoodNutrientValue, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<FoodNutrientValue, Long>
        get() = _MODIFY_TIME

    val NUTRIENT_ID: Column.Fk<FoodNutrientValue, Long, Nutrient>
        get() = _NUTRIENT_ID
    val VALUE: Column<FoodNutrientValue, Double>
        get() = _VALUE
    val CONSTRAINT_SPEC: Column<FoodNutrientValue, Int>
        get() = _CONSTRAINT_SPEC
    val UNIT_ID: Column.Fk<FoodNutrientValue, Long, Unit>
        get() = _UNIT_ID

    val FOOD_ID: Column.Fk<FoodNutrientValue, Long, Food>
        get() = _FOOD_ID
    val VERSION: Column<FoodNutrientValue, Int>
        get() = _VERSION
}
