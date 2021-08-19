package com.machfour.macros.orm.schema

import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.RegularMeal

class RegularMealTable private constructor() : TableImpl<RegularMeal>(TABLE_NAME, RegularMeal.factory, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "RegularMeal"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<RegularMeal, *>>()

        val ID: Column<RegularMeal, Long>
        val CREATE_TIME: Column<RegularMeal, Long>
        val MODIFY_TIME: Column<RegularMeal, Long>
        val NAME: Column<RegularMeal, String>
        val MEAL_ID: Column.Fk<RegularMeal, Long, Meal>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            NAME = SchemaHelpers.builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
            MEAL_ID = SchemaHelpers.builder("meal_id", Types.ID).notEditable().notNull().inSecondaryKey().unique()
                .buildAndAddFk(MealTable.ID, MealTable.instance, COLUMNS)
        }

        // declaration has to be last (static initialisation order)
        val instance = RegularMealTable()
    }
}
