package com.machfour.macros.sample

import com.machfour.macros.sql.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.NutrientGoalTable
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.inbuilt.Nutrients.CARBOHYDRATE
import com.machfour.macros.entities.inbuilt.Nutrients.ENERGY
import com.machfour.macros.entities.inbuilt.Nutrients.FAT
import com.machfour.macros.entities.inbuilt.Nutrients.PROTEIN
import com.machfour.macros.nutrientdata.FoodNutrientData
import com.machfour.macros.entities.inbuilt.Nutrients.FIBRE
import com.machfour.macros.entities.inbuilt.Units
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.entities.inbuilt.Nutrients.WATER


object ExampleNutrientData {

    val foodNd = FoodNutrientData().apply {
        this[ENERGY] = FoodNutrientValue.makeComputedValue(1000.0, ENERGY, Units.CALORIES)
        this[PROTEIN] = FoodNutrientValue.makeComputedValue(200.0, PROTEIN, Units.GRAMS)
        this[FAT] = FoodNutrientValue.makeComputedValue(100.0, FAT, Units.GRAMS)
        this[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(220.0, CARBOHYDRATE, Units.GRAMS)
        this[FIBRE] = FoodNutrientValue.makeComputedValue(80.0, FIBRE, Units.GRAMS)
        this[WATER] = FoodNutrientValue.makeComputedValue(550.0, WATER, Units.MILLILITRES)
    }

    private val dayGoalColumnData = ColumnData(NutrientGoal.table).apply {
        put(NutrientGoalTable.NAME, "Example nutrient goal")
        put(NutrientGoalTable.ID, MacrosEntity.NO_ID)
    }

    val dayGoalNd = NutrientGoal(dayGoalColumnData, ObjectSource.COMPUTED).apply {
        addComputedValue(ENERGY, 2000.0, Units.CALORIES)
        addComputedValue(PROTEIN, 400.0, Units.GRAMS)
        addComputedValue(FAT, 65.0, Units.GRAMS)
        addComputedValue(CARBOHYDRATE, 300.0, Units.GRAMS)
        addComputedValue(FIBRE, 80.0, Units.GRAMS)
        addComputedValue(WATER, 1000.0, Units.MILLILITRES)
    }
}
