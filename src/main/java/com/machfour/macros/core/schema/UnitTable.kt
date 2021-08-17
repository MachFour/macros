package com.machfour.macros.core.schema

import com.machfour.macros.orm.BaseTable
import com.machfour.macros.orm.Column
import com.machfour.macros.orm.datatype.Types
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories

// Unit.factory() causes initialisation of Unit, which depends on this class.
// So the columns are initialised as a side effect of calling that function.
class UnitTable private constructor() : BaseTable<Unit>(TABLE_NAME, Factories.unit, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "Unit"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<Unit, *>>()

        // initialisation order here is the order of iteration of the columns
        val ID: Column<Unit, Long>
        val CREATE_TIME: Column<Unit, Long>
        val MODIFY_TIME: Column<Unit, Long>
        val TYPE_ID: Column<Unit, Int>
        val NAME: Column<Unit, String>
        val ABBREVIATION: Column<Unit, String>
        val METRIC_EQUIVALENT: Column<Unit, Double>
        val INBUILT: Column<Unit, Boolean>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            NAME = SchemaHelpers.builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
            TYPE_ID = SchemaHelpers.builder("type_id", Types.INTEGER).notEditable().notNull().buildAndAdd(COLUMNS)
            ABBREVIATION = SchemaHelpers.builder("abbreviation", Types.TEXT).notNull().inSecondaryKey().unique().buildAndAdd(COLUMNS)
            METRIC_EQUIVALENT = SchemaHelpers.builder("metric_equivalent", Types.REAL).notNull().buildAndAdd(COLUMNS)
            INBUILT = SchemaHelpers.builder("inbuilt", Types.BOOLEAN).notNull().defaultsTo(false).buildAndAdd(COLUMNS)
        }

        // this declaration has to come last (static initialisation order
        val instance = UnitTable()
    }
}

