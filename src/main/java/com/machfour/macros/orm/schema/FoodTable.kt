package com.machfour.macros.orm.schema

import com.machfour.macros.core.FoodType
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "Food"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<Food, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NOTES = notesColumnBuildAndAdd(COLUMNS)

private val _INDEX_NAME =
    builder("index_name", Types.TEXT).notNull().unique().inSecondaryKey().buildFor(COLUMNS)
private val _NAME =
    builder("name", Types.TEXT).buildFor(COLUMNS)
private val _BRAND =
    builder("brand", Types.TEXT).buildFor(COLUMNS)
private val _VARIETY =
    builder("variety", Types.TEXT).buildFor(COLUMNS)
private val _EXTRA_DESC =
    builder("extra_desc", Types.TEXT).buildFor(COLUMNS)
private val _CATEGORY =
    builder("category", Types.TEXT).buildFkFor(FoodCategoryTable, FoodCategoryTable.NAME, COLUMNS)
private val _FOOD_TYPE =
    builder("food_type", Types.TEXT).notEditable().notNull().defaultsTo(FoodType.PRIMARY.niceName).buildFor(COLUMNS)
private val _USDA_INDEX =
    builder("usda_index", Types.INTEGER).notEditable().buildFor(COLUMNS)
private val _NUTTAB_INDEX =
    builder("nuttab_index", Types.TEXT).notEditable().buildFor(COLUMNS)
private val _DATA_SOURCE =
    builder("data_source", Types.TEXT).buildFor(COLUMNS)
private val _DATA_NOTES =
    builder("data_notes", Types.TEXT).buildFor(COLUMNS)
private val _DENSITY =
    builder("density", Types.REAL).buildFor(COLUMNS)
private val _SEARCH_RELEVANCE =
    builder("search_relevance", Types.INTEGER).notEditable().defaultsTo(0).buildFor(COLUMNS)

object FoodTable: TableImpl<Food>(TABLE_NAME, Factories.food, COLUMNS) {
    val ID: Column<Food, Long>
        get() =_ID
    val CREATE_TIME: Column<Food, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<Food, Long>
        get() = _MODIFY_TIME
    val INDEX_NAME: Column<Food, String>
        get() = _INDEX_NAME
    val BRAND: Column<Food, String>
        get() = _BRAND
    val VARIETY: Column<Food, String>
        get() = _VARIETY
    val EXTRA_DESC: Column<Food, String>
        get() = _EXTRA_DESC
    val NAME: Column<Food, String>
        get() = _NAME
    val NOTES: Column<Food, String>
        get() = _NOTES
    val FOOD_TYPE: Column<Food, String>
        get() = _FOOD_TYPE
    val USDA_INDEX: Column<Food, Int>
        get() = _USDA_INDEX
    val NUTTAB_INDEX: Column<Food, String>
        get() = _NUTTAB_INDEX
    val DATA_SOURCE: Column<Food, String>
        get() = _DATA_SOURCE
    val DATA_NOTES: Column<Food, String>
        get() = _DATA_NOTES
    val DENSITY: Column<Food, Double>
        get() = _DENSITY
    val SEARCH_RELEVANCE: Column<Food, Int>
        get() = _SEARCH_RELEVANCE
    val CATEGORY: Column.Fk<Food, String, FoodCategory>
        get() = _CATEGORY
}
