package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.core.Schema.FoodTable
import com.machfour.macros.objects.Nutrient
import com.machfour.macros.objects.inbuilt.Nutrients

// Singleton pattern
class EnglishColumnNames private constructor(): ColumnNamer {
    companion object {
        // shortest name for each nutrient printed in default mode
        val briefNutrientNames: Map<Nutrient, String> = mapOf(
                  Nutrients.ENERGY to "Energy"
                , Nutrients.PROTEIN to "Prot"
                , Nutrients.FAT to "Fat"
                , Nutrients.CARBOHYDRATE to "Carb"
                , Nutrients.QUANTITY to "Qty"
        )

        // longer (but really medium name for nutrients printed in verbose mode
        val longerNutrientNames = mapOf(
                  Nutrients.ENERGY to "Energy"
                , Nutrients.PROTEIN to "Protein"
                , Nutrients.FAT to "Fat"
                , Nutrients.SATURATED_FAT to "Sat Fat"
                , Nutrients.CARBOHYDRATE to "Carbs"
                , Nutrients.SUGAR to "Sugar"
                , Nutrients.FIBRE to "Fibre"
                , Nutrients.SODIUM to "Sodium"
                , Nutrients.CALCIUM to "Calcium"
                , Nutrients.QUANTITY to "Qty"
        )

        private val nutrientNames = mapOf(
            // Nutrient columns
              Nutrients.ENERGY to "Energy"
            , Nutrients.PROTEIN to "Protein"
            , Nutrients.FAT to "Fat"
            , Nutrients.SATURATED_FAT to "Saturated Fat"
            , Nutrients.CARBOHYDRATE to "Carbohydrate"
            , Nutrients.SUGAR to "Sugars"
            , Nutrients.FIBRE to "Fibre"
            , Nutrients.SODIUM to "Sodium"
            , Nutrients.CALCIUM to "Calcium"
            , Nutrients.QUANTITY to "Quantity"
            , Nutrients.IRON to "Iron"
            , Nutrients.STARCH to "Starch"
            , Nutrients.CARBOHYDRATE_BY_DIFF to "Carbohydrate by difference"
            , Nutrients.OMEGA_3_FAT to "Omega 3"
            , Nutrients.OMEGA_6_FAT to "Omega 6"
            , Nutrients.MONOUNSATURATED_FAT to "Monounsaturated Fat"
            , Nutrients.POLYUNSATURATED_FAT to "Polyunsaturated Fat"
            , Nutrients.SUGAR_ALCOHOL to "Sugar Alcohol"
            , Nutrients.WATER to "Water"
            , Nutrients.POTASSIUM to "Potassium"
            , Nutrients.SALT to "Salt"
            , Nutrients.ALCOHOL to "Alcohol"
            , Nutrients.CAFFEINE to "Caffeine"
        )

        // TODO put ALLLLLLLLLLL columns in here
        private val columnNames: Map<Column<*, *>, String> = mapOf(
                // Food Table
                  FoodTable.ID to "ID"
                , FoodTable.CREATE_TIME to "Creation time"
                , FoodTable.MODIFY_TIME to "Last modified"
                , FoodTable.INDEX_NAME to "Index name"
                , FoodTable.BRAND to "Brand"
                , FoodTable.VARIETY to "Variety"
                , FoodTable.EXTRA_DESC to "Extra description"
                , FoodTable.NAME to "Name"
                , FoodTable.NOTES to "Notes"
                , FoodTable.FOOD_TYPE to "Food Type"
                , FoodTable.USDA_INDEX to "USDA DB index"
                , FoodTable.NUTTAB_INDEX to "NUTTAB DB index"
                , FoodTable.CATEGORY to "Category"
                , FoodTable.DATA_NOTES to "Data Notes"
                , FoodTable.DATA_SOURCE to "Data Source"
                , FoodTable.DENSITY to "Density (g/mL)"

        )

        val instance: EnglishColumnNames = EnglishColumnNames()
    }

    override fun getName(col: Column<*, *>): String {
        return columnNames[col]
                ?: throw UnsupportedOperationException("Name for $col not yet added, sorry!")
    }
    override fun getName(n: Nutrient): String {
        return nutrientNames[n]
                ?: throw UnsupportedOperationException("Name for $n not yet added, sorry!")
    }

}
