package com.machfour.macros.core.schema

import com.machfour.macros.orm.Table

// Just a list of all tables, in a separate file to avoid static initialisation order issues
object Tables {

    val all: List<Table<*>> = listOf(
        AttrMappingTable.instance,
        FoodAttributeTable.instance,
        FoodCategoryTable.instance,
        FoodNutrientValueTable.instance,
        FoodPortionTable.instance,
        FoodTable.instance,
        IngredientTable.instance,
        MealTable.instance,
        NutrientGoalDayMappingTable.instance,
        NutrientGoalTable.instance,
        NutrientGoalValueTable.instance,
        NutrientTable.instance,
        RegularMealTable.instance,
        ServingTable.instance,
        UnitTable.instance,
    )
}