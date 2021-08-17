package com.machfour.macros.core.schema

import com.machfour.macros.orm.BaseTable
import com.machfour.macros.orm.Column
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.orm.datatype.Types
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Serving
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories

class ServingTable private constructor() : BaseTable<Serving>(TABLE_NAME, Factories.serving, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "Serving"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<Serving, *>>()

        val ID: Column<Serving, Long>
        val CREATE_TIME: Column<Serving, Long>
        val MODIFY_TIME: Column<Serving, Long>
        val NAME: Column<Serving, String>
        val QUANTITY: Column<Serving, Double>
        val IS_DEFAULT: Column<Serving, Boolean>
        val FOOD_ID: Column.Fk<Serving, Long, Food>
        val QUANTITY_UNIT: Column.Fk<Serving, String, Unit>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            NAME = SchemaHelpers.builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
            QUANTITY = SchemaHelpers.builder("quantity", Types.REAL).notNull().buildAndAdd(COLUMNS)
            QUANTITY_UNIT = SchemaHelpers.builder("quantity_unit", Types.TEXT).notNull()
                .buildAndAddFk(UnitTable.ABBREVIATION, UnitTable.instance, COLUMNS)
            IS_DEFAULT = SchemaHelpers.builder("is_default", Types.NULLBOOLEAN).notNull().defaultsTo(false).buildAndAdd(COLUMNS)
            FOOD_ID = SchemaHelpers.builder("food_id", Types.ID)
                .notEditable().notNull().defaultsTo(MacrosEntity.NO_ID)
                .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
        }

        // this declaration has to be last (static initialisation order)
        val instance = ServingTable()
    }
}
