package com.machfour.macros.sample

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.NutrientGoalTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.CALORIES
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLILITRES

val exampleFoodNd by lazy {
    FoodNutrientData().apply {
        this[com.machfour.macros.nutrients.ENERGY] =
            FoodNutrientValue.makeComputedValue(
                1000.0,
                com.machfour.macros.nutrients.ENERGY,
                CALORIES
            )
        this[com.machfour.macros.nutrients.PROTEIN] =
            FoodNutrientValue.makeComputedValue(200.0, com.machfour.macros.nutrients.PROTEIN, GRAMS)
        this[com.machfour.macros.nutrients.FAT] =
            FoodNutrientValue.makeComputedValue(100.0, com.machfour.macros.nutrients.FAT, GRAMS)
        this[com.machfour.macros.nutrients.CARBOHYDRATE] =
            FoodNutrientValue.makeComputedValue(
                220.0,
                com.machfour.macros.nutrients.CARBOHYDRATE,
                GRAMS
            )
        this[com.machfour.macros.nutrients.FIBRE] =
            FoodNutrientValue.makeComputedValue(80.0, com.machfour.macros.nutrients.FIBRE, GRAMS)
        this[com.machfour.macros.nutrients.WATER] =
            FoodNutrientValue.makeComputedValue(
                550.0,
                com.machfour.macros.nutrients.WATER,
                MILLILITRES
            )
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
        addComputedValue(com.machfour.macros.nutrients.ENERGY, 2000.0, CALORIES)
        addComputedValue(com.machfour.macros.nutrients.PROTEIN, 400.0, GRAMS)
        addComputedValue(com.machfour.macros.nutrients.FAT, 65.0, GRAMS)
        addComputedValue(com.machfour.macros.nutrients.CARBOHYDRATE, 300.0, GRAMS)
        addComputedValue(com.machfour.macros.nutrients.FIBRE, 80.0, GRAMS)
        addComputedValue(com.machfour.macros.nutrients.WATER, 1000.0, MILLILITRES)
    }
}