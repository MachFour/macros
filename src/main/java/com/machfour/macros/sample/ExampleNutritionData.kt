package com.machfour.macros.sample

import com.machfour.macros.objects.NutrientValue
import com.machfour.macros.objects.inbuilt.Nutrients.CARBOHYDRATE
import com.machfour.macros.objects.inbuilt.Nutrients.ENERGY
import com.machfour.macros.objects.inbuilt.Nutrients.FAT
import com.machfour.macros.objects.inbuilt.Nutrients.PROTEIN
import com.machfour.macros.core.NutrientData
import com.machfour.macros.objects.inbuilt.Units


object ExampleNutrientData {

    val nd = NutrientData().apply {
        this[ENERGY] = NutrientValue.makeComputedValue(1000.0, ENERGY, Units.CALORIES)
        this[PROTEIN] = NutrientValue.makeComputedValue(200.0, PROTEIN, Units.GRAMS)
        this[FAT] = NutrientValue.makeComputedValue(100.0, FAT, Units.GRAMS)
        this[CARBOHYDRATE] = NutrientValue.makeComputedValue(220.0, CARBOHYDRATE, Units.GRAMS)
    }
}
