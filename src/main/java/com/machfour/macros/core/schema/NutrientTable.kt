package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.auxiliary.Factories

class NutrientTable private constructor() : BaseTable<Nutrient>(TABLE_NAME, Factories.nutrient, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "Nutrient"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<Nutrient, *>>()

        val ID: Column<Nutrient, Long>
        val CREATE_TIME: Column<Nutrient, Long>
        val MODIFY_TIME: Column<Nutrient, Long>
        val NAME: Column<Nutrient, String>
        val UNIT_TYPES: Column<Nutrient, Int>
        val INBUILT: Column<Nutrient, Boolean>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            NAME = SchemaHelpers.builder("name", Types.TEXT).notNull().unique().inSecondaryKey().buildAndAdd(COLUMNS)
            UNIT_TYPES = SchemaHelpers.builder("unit_types", Types.INTEGER).notNull().buildAndAdd(COLUMNS)
            INBUILT = SchemaHelpers.builder("inbuilt", Types.BOOLEAN).notNull().defaultsTo(false).buildAndAdd(COLUMNS)
        }

        // this part has to be last (static initialisation order)
        val instance = NutrientTable()
    }

}

