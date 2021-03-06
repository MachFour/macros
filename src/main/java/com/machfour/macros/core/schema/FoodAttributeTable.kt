package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.objects.FoodAttribute

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
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            NAME = SchemaHelpers.builder("name", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS)
        }

        // this declaration has to be last (static initialisation order)
        val instance = FoodAttributeTable()
    }
}

