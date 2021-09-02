package com.machfour.macros.orm.schema

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Serving
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "Serving"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<Serving, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NAME =
   builder("name", Types.TEXT).notNull().buildFor(COLUMNS)
private val _QUANTITY =
   builder("quantity", Types.REAL).notNull().buildFor(COLUMNS)
private val _QUANTITY_UNIT =
   builder("quantity_unit", Types.TEXT).notNull().buildFkFor(UnitTable, UnitTable.ABBREVIATION, COLUMNS)
private val _IS_DEFAULT =
   builder("is_default", Types.NULLBOOLEAN).notNull().defaultsTo(false).buildFor(COLUMNS)
private val _FOOD_ID =
   builder("food_id", Types.ID).notEditable().notNull().defaultsTo(MacrosEntity.NO_ID)
      .buildFkFor(FoodTable, FoodTable.ID, COLUMNS)

object ServingTable: TableImpl<Serving>(TABLE_NAME, Factories.serving, COLUMNS) {

   val ID: Column<Serving, Long>
      get() = _ID
   val CREATE_TIME: Column<Serving, Long>
      get() = _CREATE_TIME
   val MODIFY_TIME: Column<Serving, Long>
      get() = _MODIFY_TIME
   val NAME: Column<Serving, String>
      get() = _NAME
   val QUANTITY: Column<Serving, Double>
      get() = _QUANTITY
   val IS_DEFAULT: Column<Serving, Boolean>
      get() = _IS_DEFAULT
   val FOOD_ID: Column.Fk<Serving, Long, Food>
      get() = _FOOD_ID
   val QUANTITY_UNIT: Column.Fk<Serving, String, Unit>
      get() = _QUANTITY_UNIT

}
