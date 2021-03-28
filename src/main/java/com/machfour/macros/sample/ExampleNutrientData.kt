package com.machfour.macros.sample

import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.inbuilt.Nutrients.CARBOHYDRATE
import com.machfour.macros.entities.inbuilt.Nutrients.ENERGY
import com.machfour.macros.entities.inbuilt.Nutrients.FAT
import com.machfour.macros.entities.inbuilt.Nutrients.PROTEIN
import com.machfour.macros.nutrientdata.FoodNutrientData
import com.machfour.macros.entities.NutrientGoalValue
import com.machfour.macros.entities.inbuilt.Nutrients.FIBRE
import com.machfour.macros.entities.inbuilt.Units
import com.machfour.macros.nutrientdata.NutrientGoal
import com.machfour.macros.util.DateStamp


object ExampleNutrientData {

    val foodNd = FoodNutrientData().apply {
        this[ENERGY] = FoodNutrientValue.makeComputedValue(1000.0, ENERGY, Units.CALORIES)
        this[PROTEIN] = FoodNutrientValue.makeComputedValue(200.0, PROTEIN, Units.GRAMS)
        this[FAT] = FoodNutrientValue.makeComputedValue(100.0, FAT, Units.GRAMS)
        this[CARBOHYDRATE] = FoodNutrientValue.makeComputedValue(220.0, CARBOHYDRATE, Units.GRAMS)
        this[FIBRE] = FoodNutrientValue.makeComputedValue(80.0, FIBRE, Units.GRAMS)
    }

    val dayGoalNd = DateStamp.currentDate().let {
        NutrientGoal(it).apply {
            this[ENERGY] = NutrientGoalValue.makeComputedValue(it, 2000.0, ENERGY, Units.CALORIES)
            this[PROTEIN] = NutrientGoalValue.makeComputedValue(it, 400.0, PROTEIN, Units.GRAMS)
            this[FAT] = NutrientGoalValue.makeComputedValue(it, 65.0, FAT, Units.GRAMS)
            this[CARBOHYDRATE] = NutrientGoalValue.makeComputedValue(it, 300.0, CARBOHYDRATE, Units.GRAMS)
            this[FIBRE] = NutrientGoalValue.makeComputedValue(it, 80.0, FIBRE, Units.GRAMS)
        }
    }
}
