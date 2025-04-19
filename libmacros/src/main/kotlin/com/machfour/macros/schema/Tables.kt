package com.machfour.macros.schema

import com.machfour.macros.sql.entities.MacrosSqlEntity
import com.machfour.macros.sql.Table

// Just a list of all tables, in a separate file to avoid static initialisation order issues
val AllTables: List<Table<out MacrosSqlEntity<*>>> = listOf(
    AttrMappingTable,
    FoodAttributeTable,
    FoodCategoryTable,
    FoodNutrientValueTable,
    FoodPortionTable,
    FoodTable,
    IngredientTable,
    MealTable,
    NutrientGoalDayMappingTable,
    NutrientGoalTable,
    NutrientGoalValueTable,
    NutrientTable,
    RegularMealTable,
    ServingTable,
    UnitTable,
)