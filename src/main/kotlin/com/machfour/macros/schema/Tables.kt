package com.machfour.macros.schema

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.Table

// Just a list of all tables, in a separate file to avoid static initialisation order issues
val AllTables: List<Table<out MacrosEntity<*>>> = listOf(
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