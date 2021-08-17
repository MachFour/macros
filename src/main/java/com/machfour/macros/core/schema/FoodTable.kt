package com.machfour.macros.core.schema

import com.machfour.macros.orm.BaseTable
import com.machfour.macros.orm.Column
import com.machfour.macros.orm.datatype.Types
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.entities.FoodType
import com.machfour.macros.entities.auxiliary.Factories

class FoodTable private constructor() : BaseTable<Food>(TABLE_NAME, Factories.food, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "Food"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<Food, *>>()

        val ID: Column<Food, Long>
        val CREATE_TIME: Column<Food, Long>
        val MODIFY_TIME: Column<Food, Long>
        val INDEX_NAME: Column<Food, String>
        val BRAND: Column<Food, String>
        val VARIETY: Column<Food, String>
        val EXTRA_DESC: Column<Food, String>
        val NAME: Column<Food, String>
        val NOTES: Column<Food, String>
        val FOOD_TYPE: Column<Food, String>
        val USDA_INDEX: Column<Food, Int>
        val NUTTAB_INDEX: Column<Food, String>
        val DATA_SOURCE: Column<Food, String>
        val DATA_NOTES: Column<Food, String>
        val DENSITY: Column<Food, Double>
        val SEARCH_RELEVANCE: Column<Food, Int>
        val CATEGORY: Column.Fk<Food, String, FoodCategory>

        init {
            // order of initialisation is order that columns will be iterated through
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            NOTES = SchemaHelpers.notesColumnBuildAndAdd(COLUMNS)

            INDEX_NAME = SchemaHelpers.builder("index_name", Types.TEXT)
                .notNull().unique().inSecondaryKey()
                .buildAndAdd(COLUMNS)
            NAME = SchemaHelpers.builder("name", Types.TEXT)
                .buildAndAdd(COLUMNS)
            BRAND = SchemaHelpers.builder("brand", Types.TEXT)
                .buildAndAdd(COLUMNS)
            VARIETY = SchemaHelpers.builder("variety", Types.TEXT)
                .buildAndAdd(COLUMNS)
            EXTRA_DESC = SchemaHelpers.builder("extra_desc", Types.TEXT)
                .buildAndAdd(COLUMNS)
            CATEGORY = SchemaHelpers.builder("category", Types.TEXT)
                .buildAndAddFk(FoodCategoryTable.NAME, FoodCategoryTable.instance, COLUMNS)
            FOOD_TYPE = SchemaHelpers.builder("food_type", Types.TEXT)
                .notEditable().notNull().defaultsTo(FoodType.PRIMARY.niceName)
                .buildAndAdd(COLUMNS)
            USDA_INDEX = SchemaHelpers.builder("usda_index", Types.INTEGER)
                .notEditable()
                .buildAndAdd(COLUMNS)
            NUTTAB_INDEX = SchemaHelpers.builder("nuttab_index", Types.TEXT)
                .notEditable()
                .buildAndAdd(COLUMNS)
            DATA_SOURCE = SchemaHelpers.builder("data_source", Types.TEXT).buildAndAdd(COLUMNS)
            DATA_NOTES = SchemaHelpers.builder("data_notes", Types.TEXT).buildAndAdd(COLUMNS)
            DENSITY = SchemaHelpers.builder("density", Types.REAL).buildAndAdd(COLUMNS)
            SEARCH_RELEVANCE = SchemaHelpers.builder("search_relevance", Types.INTEGER)
                .notEditable().defaultsTo(0)
                .buildAndAdd(COLUMNS)
        }

        // this has to come last (static initialisation order)
        val instance = FoodTable()
    }

}
