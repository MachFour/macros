package com.machfour.macros.sample

import com.machfour.macros.objects.NutrientValue
import com.machfour.macros.objects.inbuilt.Nutrients.CARBOHYDRATE
import com.machfour.macros.objects.inbuilt.Nutrients.ENERGY
import com.machfour.macros.objects.inbuilt.Nutrients.FAT
import com.machfour.macros.objects.inbuilt.Nutrients.PROTEIN
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.inbuilt.Units


object ExampleNutritionData {

    val nd = NutritionData().apply {
        nutrientData[ENERGY] = NutrientValue.makeObject(1000.0, ENERGY, Units.CALORIES)
        nutrientData[PROTEIN] = NutrientValue.makeObject(1000.0, PROTEIN, Units.GRAMS)
        nutrientData[FAT] = NutrientValue.makeObject(1000.0, FAT, Units.GRAMS)
        nutrientData[CARBOHYDRATE] = NutrientValue.makeObject(1000.0, CARBOHYDRATE, Units.GRAMS)
    }
}
