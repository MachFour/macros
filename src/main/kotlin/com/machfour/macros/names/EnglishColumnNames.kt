package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.core.Schema.FoodTable
import com.machfour.macros.core.Schema.NutritionDataTable
import com.machfour.macros.objects.NutritionData

// Singleton pattern
class EnglishColumnNames private constructor(): ColumnNamer {
    companion object {
        // shortest name for each nutrient printed in default mode
        val briefNutrientNames: Map<Column<NutritionData, Double>, String> = mapOf(
                  NutritionDataTable.CALORIES to "Cals"
                , NutritionDataTable.PROTEIN to "Prot"
                , NutritionDataTable.FAT to "Fat"
                , NutritionDataTable.CARBOHYDRATE to "Carb"
                , NutritionDataTable.QUANTITY to "Qty"
        )

        // longer (but really medium name for nutrients printed in verbose mode
        val longerNutrientNames: Map<Column<NutritionData, Double>, String> = mapOf(
                  NutritionDataTable.CALORIES to "Cals"
                , NutritionDataTable.PROTEIN to "Protæn"
                , NutritionDataTable.FAT to "Fat"
                , NutritionDataTable.SATURATED_FAT to "Sat Fat"
                , NutritionDataTable.CARBOHYDRATE to "Carbs"
                , NutritionDataTable.SUGAR to "Sugar"
                , NutritionDataTable.FIBRE to "Fibre"
                , NutritionDataTable.SODIUM to "Sodium"
                , NutritionDataTable.CALCIUM to "Ca"
                , NutritionDataTable.QUANTITY to "Qty"
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

                // NutritionData Table
                , NutritionDataTable.ID to "ID"
                , NutritionDataTable.CREATE_TIME to "Creation time"
                , NutritionDataTable.MODIFY_TIME to "Last modified"
                , NutritionDataTable.DATA_SOURCE to "Data source"
                , NutritionDataTable.DENSITY to "Density"
                , NutritionDataTable.FOOD_ID to "Food ID"
                , NutritionDataTable.QUANTITY_UNIT to "Quantity Unit"

                // Nutrient columns
                , NutritionDataTable.KILOJOULES to "Kilojoules"
                , NutritionDataTable.CALORIES to "Calories"
                , NutritionDataTable.PROTEIN to "Protein"
                , NutritionDataTable.FAT to "Fat"
                , NutritionDataTable.SATURATED_FAT to "Saturated Fat"
                , NutritionDataTable.CARBOHYDRATE to "Carbohydrate"
                , NutritionDataTable.SUGAR to "Sugars"
                , NutritionDataTable.FIBRE to "Fibre"
                , NutritionDataTable.SODIUM to "Sodium"
                , NutritionDataTable.CALCIUM to "Calcium"
                , NutritionDataTable.QUANTITY to "Quantity"
                , NutritionDataTable.IRON to "Iron"
                , NutritionDataTable.STARCH to "Starch"
                , NutritionDataTable.CARBOHYDRATE_BY_DIFF to "Carbohydrate by difference"
                , NutritionDataTable.OMEGA_3_FAT to "Omega 3"
                , NutritionDataTable.OMEGA_6_FAT to "Omega 6"
                , NutritionDataTable.MONOUNSATURATED_FAT to "Monounsaturated Fat"
                , NutritionDataTable.POLYUNSATURATED_FAT to "Polyunsaturated Fat"
                , NutritionDataTable.SUGAR_ALCOHOL to "Sugar Alcohol"
                , NutritionDataTable.WATER to "Water"
                , NutritionDataTable.POTASSIUM to "Potassium"
                , NutritionDataTable.SALT to "Salt"
                , NutritionDataTable.ALCOHOL to "Alcohol"
        )

        val instance: EnglishColumnNames = EnglishColumnNames()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getName(col: Column<*, *>): String {
        return columnNames[col]
                ?: throw UnsupportedOperationException("Name for $col not yet added, sorry!")
    }

}