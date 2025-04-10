package com.machfour.macros.schema

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.TableImpl
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.datatype.Types

private const val tableName = "FoodNutrientValue"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<FoodNutrientValue, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)

private val nutrientId = nutrientValueNutrientColumn(columns)
private val unitId = nutrientValueUnitColumn(columns)
private val value = nutrientValueAmountColumn(columns)
private val constraintSpec = nutrientValueConstraintColumn(columns)

// might have NO_ID value if it's not being stored in the database (i.e. computed value)
private val foodId =
    builder("food_id", Types.ID).notNull().notEditable().defaultsTo(MacrosEntity.NO_ID)
        .buildFkFor(FoodTable, FoodTable.ID, columns)
private val version =
    builder("version", Types.INTEGER).notNull().defaultsTo(1).buildFor(columns)

object FoodNutrientValueTable: TableImpl<FoodNutrientValue>(tableName, Factories.foodNutrientValue, columns) {
    val ID: Column<FoodNutrientValue, Long>
        get() = id
    val CREATE_TIME: Column<FoodNutrientValue, Long>
        get() = createTime
    val MODIFY_TIME: Column<FoodNutrientValue, Long>
        get() = modifyTime

    val NUTRIENT_ID: Column.Fk<FoodNutrientValue, Long, Nutrient>
        get() = nutrientId
    val VALUE: Column<FoodNutrientValue, Double>
        get() = value
    val CONSTRAINT_SPEC: Column<FoodNutrientValue, Int>
        get() = constraintSpec
    val UNIT_ID: Column.Fk<FoodNutrientValue, Long, Unit>
        get() = unitId

    val FOOD_ID: Column.Fk<FoodNutrientValue, Long, Food>
        get() = foodId
    val VERSION: Column<FoodNutrientValue, Int>
        get() = version
}
