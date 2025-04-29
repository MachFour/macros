package com.machfour.macros.schema

import com.machfour.macros.core.EntityId
import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.sql.entities.Factories

private const val tableName = "Nutrient"

private val columns = ArrayList<Column<Nutrient, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val name = builder("name", Types.TEXT).notNull().unique().buildFor(columns)
private val unitTypes = builder("unit_types", Types.INTEGER).notNull().buildFor(columns)
private val inbuilt = builder("inbuilt", Types.BOOLEAN).notNull().default { false }.buildFor(columns)

object NutrientTable : TableImpl<INutrient, Nutrient>(tableName, Factories.nutrient, columns) {
    val ID: Column<Nutrient, EntityId>
        get() = id
    val CREATE_TIME: Column<Nutrient, Long>
        get() = createTime
    val MODIFY_TIME: Column<Nutrient, Long>
        get() = modifyTime
    val NAME: Column<Nutrient, String>
        get() = name
    val UNIT_TYPES: Column<Nutrient, Int>
        get() = unitTypes
    val INBUILT: Column<Nutrient, Boolean>
        get() = inbuilt

}

