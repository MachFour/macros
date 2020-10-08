package com.machfour.macros.sample

import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.Schema
import com.machfour.macros.objects.NutritionData


object ExampleNutritionData {

    val nd = MacrosBuilder(NutritionData.table).run {
        setField(Schema.NutritionDataTable.CALORIES, 1000.0)
        setField(Schema.NutritionDataTable.PROTEIN, 100.0)
        setField(Schema.NutritionDataTable.CARBOHYDRATE, 200.0)
        setField(Schema.NutritionDataTable.FAT, 80.0)
        build()
    }
}