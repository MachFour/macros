package com.machfour.macros.schema

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.FoodType
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.entities.FoodImpl
import com.machfour.macros.entities.IFood
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val tableName = "Food"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<Food, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)

private val indexName =
    builder("index_name", Types.TEXT).notNull().unique().buildFor(columns)
private val name =
    builder("name", Types.TEXT).buildFor(columns)
private val brand =
    builder("brand", Types.TEXT).buildFor(columns)
private val variety =
    builder("variety", Types.TEXT).buildFor(columns)
private val extraDesc =
    builder("extra_desc", Types.TEXT).buildFor(columns)
private val category =
    builder("category", Types.TEXT).buildFkFor(FoodCategoryTable.NAME, columns)
private val notes =
    notesColumnBuildAndAdd(columns)
private val foodType =
    builder("food_type", Types.TEXT).notEditable().notNull().default { FoodType.PRIMARY.niceName }.buildFor(columns)
private val usdaIndex =
    builder("usda_index", Types.INTEGER).notEditable().unique().buildFor(columns)
private val nuttabIndex =
    builder("nuttab_index", Types.TEXT).notEditable().unique().buildFor(columns)
private val dataSource =
    builder("data_source", Types.TEXT).buildFor(columns)
private val dataNotes =
    builder("data_notes", Types.TEXT).buildFor(columns)
private val density =
    builder("density", Types.REAL).buildFor(columns)
private val searchRelevance =
    builder("search_relevance", Types.INTEGER).notEditable().buildFor(columns)

object FoodTable : TableImpl<IFood<*>, Food>(tableName, FoodImpl.factory, columns) {
    val ID: Column<Food, EntityId>
        get() = id
    val CREATE_TIME: Column<Food, Long>
        get() = createTime
    val MODIFY_TIME: Column<Food, Long>
        get() = modifyTime
    val INDEX_NAME: Column<Food, String>
        get() = indexName
    val BRAND: Column<Food, String>
        get() = brand
    val VARIETY: Column<Food, String>
        get() = variety
    val EXTRA_DESC: Column<Food, String>
        get() = extraDesc
    val NAME: Column<Food, String>
        get() = name
    val NOTES: Column<Food, String>
        get() = notes
    val FOOD_TYPE: Column<Food, String>
        get() = foodType
    val USDA_INDEX: Column<Food, Int>
        get() = usdaIndex
    val NUTTAB_INDEX: Column<Food, String>
        get() = nuttabIndex
    val DATA_SOURCE: Column<Food, String>
        get() = dataSource
    val DATA_NOTES: Column<Food, String>
        get() = dataNotes
    val DENSITY: Column<Food, Double>
        get() = density
    val SEARCH_RELEVANCE: Column<Food, Int>
        get() = searchRelevance
    val CATEGORY: Column.Fk<Food, String, FoodCategory>
        get() = category
}
