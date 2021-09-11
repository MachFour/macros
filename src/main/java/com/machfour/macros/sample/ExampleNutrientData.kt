package com.machfour.macros.sample

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.nutrients.*
import com.machfour.macros.schema.NutrientGoalTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.CALORIES
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLILITRES

val exampleFoodNd by lazy {
    FoodNutrientData().apply {
        this[ENERGY] = FoodNutrientValue.makeComputedValue(1000.0, ENERGY, CALORIES)
        this[PROTEIN] = FoodNutrientValue.makeComputedValue(200.0, PROTEIN, GRAMS)
        this[FAT] = FoodNutrientValue.makeComputedValue(100.0, FAT, GRAMS)
        this[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(220.0, CARBOHYDRATE, GRAMS)
        this[FIBRE] = FoodNutrientValue.makeComputedValue(80.0, FIBRE, GRAMS)
        this[WATER] = FoodNutrientValue.makeComputedValue(550.0, WATER, MILLILITRES)
    }
}

private val exampleDayGoalRowData by lazy {
    RowData(NutrientGoal.table).apply {
        put(NutrientGoalTable.NAME, "Example nutrient goal")
        put(NutrientGoalTable.ID, MacrosEntity.NO_ID)
    }
}

val exampleDayGoalNd by lazy {
    NutrientGoal(exampleDayGoalRowData, ObjectSource.COMPUTED).apply {
        addComputedValue(ENERGY, 2000.0, CALORIES)
        addComputedValue(PROTEIN, 400.0, GRAMS)
        addComputedValue(FAT, 65.0, GRAMS)
        addComputedValue(CARBOHYDRATE, 300.0, GRAMS)
        addComputedValue(FIBRE, 80.0, GRAMS)
        addComputedValue(WATER, 1000.0, MILLILITRES)
    }
}