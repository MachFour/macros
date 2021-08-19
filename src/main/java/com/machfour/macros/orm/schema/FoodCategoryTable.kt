package com.machfour.macros.orm.schema

import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.entities.FoodCategory

class FoodCategoryTable private constructor() : TableImpl<FoodCategory>(TABLE_NAME, FoodCategory.factory, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "FoodCategory"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<FoodCategory, *>>()

        val ID: Column<FoodCategory, Long>
        val CREATE_TIME: Column<FoodCategory, Long>
        val MODIFY_TIME: Column<FoodCategory, Long>
        val NAME: Column<FoodCategory, String>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            NAME = SchemaHelpers.builder("name", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS)
        }

        val instance = FoodCategoryTable()
    }

}
