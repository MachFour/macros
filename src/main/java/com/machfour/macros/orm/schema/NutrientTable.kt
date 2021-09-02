package com.machfour.macros.orm.schema

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "Nutrient"

private val COLUMNS = ArrayList<Column<Nutrient, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NAME = builder("name", Types.TEXT).notNull().unique().inSecondaryKey().buildFor(COLUMNS)
private val _UNIT_TYPES = builder("unit_types", Types.INTEGER).notNull().buildFor(COLUMNS)
private val _INBUILT = builder("inbuilt", Types.BOOLEAN).notNull().defaultsTo(false).buildFor(COLUMNS)

object NutrientTable: TableImpl<Nutrient>(TABLE_NAME, Factories.nutrient, COLUMNS) {
   val ID: Column<Nutrient, Long>
      get() = _ID
   val CREATE_TIME: Column<Nutrient, Long>
      get() = _CREATE_TIME
   val MODIFY_TIME: Column<Nutrient, Long>
      get() = _MODIFY_TIME
   val NAME: Column<Nutrient, String>
      get() = _NAME
   val UNIT_TYPES: Column<Nutrient, Int>
      get() = _UNIT_TYPES
   val INBUILT: Column<Nutrient, Boolean>
      get() = _INBUILT

}

