package com.machfour.macros.core.schema

import com.machfour.macros.orm.BaseTable
import com.machfour.macros.orm.Column
import com.machfour.macros.orm.datatype.Types
import com.machfour.macros.entities.AttrMapping
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodAttribute

class AttrMappingTable private constructor() : BaseTable<AttrMapping>(TABLE_NAME, AttrMapping.factory, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "AttributeMapping"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<AttrMapping, *>>()

        val ID: Column<AttrMapping, Long>
        val CREATE_TIME: Column<AttrMapping, Long>
        val MODIFY_TIME: Column<AttrMapping, Long>
        val FOOD_ID: Column.Fk<AttrMapping, Long, Food>
        val ATTRIBUTE_ID: Column.Fk<AttrMapping, Long, FoodAttribute>


        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            FOOD_ID = SchemaHelpers.builder("food_id", Types.ID).notEditable().notNull().inSecondaryKey()
                .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
            ATTRIBUTE_ID = SchemaHelpers.builder("attribute_id", Types.ID).notEditable().notNull().inSecondaryKey()
                .buildAndAddFk(FoodAttributeTable.ID, FoodAttributeTable.instance, COLUMNS)
        }

        // this declaration has to be last (static initialisation order)
        val instance = AttrMappingTable()
    }
}
