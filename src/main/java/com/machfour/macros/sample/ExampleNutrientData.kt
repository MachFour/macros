package com.machfour.macros.sample

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.Nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.Nutrients.ENERGY
import com.machfour.macros.nutrients.Nutrients.FAT
import com.machfour.macros.nutrients.Nutrients.FIBRE
import com.machfour.macros.nutrients.Nutrients.PROTEIN
import com.machfour.macros.nutrients.Nutrients.WATER
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.NutrientGoalTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.CALORIES
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.MILLILITRES


object ExampleNutrientData {

    val foodNd = FoodNutrientData().apply {
        this[ENERGY] = FoodNutrientValue.makeComputedValue(1000.0, ENERGY, CALORIES)
        this[PROTEIN] = FoodNutrientValue.makeComputedValue(200.0, PROTEIN, GRAMS)
        this[FAT] = FoodNutrientValue.makeComputedValue(100.0, FAT, GRAMS)
        this[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(220.0, CARBOHYDRATE, GRAMS)
        this[FIBRE] = FoodNutrientValue.makeComputedValue(80.0, FIBRE, GRAMS)
        this[WATER] = FoodNutrientValue.makeComputedValue(550.0, WATER, MILLILITRES)
    }

    private val dayGoalRowData = RowData(NutrientGoal.table).apply {
        put(NutrientGoalTable.NAME, "Example nutrient goal")
        put(NutrientGoalTable.ID, MacrosEntity.NO_ID)
    }

    val dayGoalNd = NutrientGoal(dayGoalRowData, ObjectSource.COMPUTED).apply {
        addComputedValue(ENERGY, 2000.0, CALORIES)
        addComputedValue(PROTEIN, 400.0, GRAMS)
        addComputedValue(FAT, 65.0, GRAMS)
        addComputedValue(CARBOHYDRATE, 300.0, GRAMS)
        addComputedValue(FIBRE, 80.0, GRAMS)
        addComputedValue(WATER, 1000.0, MILLILITRES)
    }
}
