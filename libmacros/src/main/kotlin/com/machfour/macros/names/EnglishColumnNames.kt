package com.machfour.macros.names

import com.machfour.macros.entities.INutrient
import com.machfour.macros.nutrients.*
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.Column

object EnglishColumnNames: ColumnStrings, NutrientStrings {
    // shortest name for each nutrient printed in default mode
    private val briefNutrientNames = mapOf(
        ENERGY to "Energy",
        PROTEIN to "Prot",
        FAT to "Fat",
        CARBOHYDRATE to "Carb",
        QUANTITY to "Qty"
    )

    // longer (but really medium name for nutrients printed in verbose mode)
    private val longerNutrientNames = mapOf(
        ENERGY to "Energy",
        PROTEIN to "Protein",
        FAT to "Fat",
        SATURATED_FAT to "Sat Fat",
        CARBOHYDRATE to "Carbs",
        SUGAR to "Sugar",
        FIBRE to "Fibre",
        SODIUM to "Sodium",
        CALCIUM to "Calcium",
        QUANTITY to "Qty"
    )

    private val nutrientNames = mapOf(
        // Nutrient columns
        ENERGY to "Energy",
        PROTEIN to "Protein",
        FAT to "Fat",
        SATURATED_FAT to "Saturated Fat",
        CARBOHYDRATE to "Carbohydrate",
        SUGAR to "Sugars",
        FIBRE to "Fibre",
        SODIUM to "Sodium",
        CALCIUM to "Calcium",
        QUANTITY to "Quantity",
        IRON to "Iron",
        STARCH to "Starch",
        CARBOHYDRATE_BY_DIFF to "Carbohydrate by difference",
        OMEGA_3_FAT to "Omega 3",
        OMEGA_6_FAT to "Omega 6",
        MONOUNSATURATED_FAT to "Monounsaturated Fat",
        POLYUNSATURATED_FAT to "Polyunsaturated Fat",
        SUGAR_ALCOHOL to "Sugar Alcohol",
        WATER to "Water",
        POTASSIUM to "Potassium",
        SALT to "Salt",
        ALCOHOL to "Alcohol",
        CAFFEINE to "Caffeine",
        ERYTHRITOL to "Erythritol",
        GLYCEROL to "Glycerol",
        ISOMALT to "Isomalt",
        LACTITOL to "Lactitol",
        MALTITOL to "Maltitol",
        MANNITOL to "Mannitol",
        SORBITOL to "Sorbitol",
        XYLITOL to "Xylitol",
        POLYDEXTROSE to "Polydextrose"
    )

    // TODO put ALLLLLLLLLLL columns in here
    private val columnNames = mapOf(
        // Food Table
        FoodTable.ID to "ID",
        FoodTable.CREATE_TIME to "Creation time",
        FoodTable.MODIFY_TIME to "Last modified",
        FoodTable.INDEX_NAME to "Index name",
        FoodTable.BRAND to "Brand",
        FoodTable.VARIETY to "Variety",
        FoodTable.EXTRA_DESC to "Extra description",
        FoodTable.NAME to "Name",
        FoodTable.NOTES to "Notes",
        FoodTable.FOOD_TYPE to "Food Type",
        FoodTable.USDA_INDEX to "USDA DB index",
        FoodTable.NUTTAB_INDEX to "NUTTAB DB index",
        FoodTable.CATEGORY to "Category",
        FoodTable.DATA_NOTES to "Data Notes",
        FoodTable.DATA_SOURCE to "Data Source",
        FoodTable.DENSITY to "Density (g/mL)",
    )

    override fun getFullName(col: Column<*, *>): String {
        return columnNames[col] ?: TODO("Name for $col not yet added, sorry!")
    }

    override fun getAbbreviatedName(col: Column<*, *>): String {
        return getFullName(col)
    }

    override fun getFullName(n: INutrient): String {
        return nutrientNames[n] ?: TODO("Name for $n not yet added, sorry!")
    }

    override fun getDisplayName(n: INutrient): String {
        return longerNutrientNames[n] ?: getFullName(n)
    }

    override fun getAbbreviatedName(n: INutrient): String {
        return briefNutrientNames[n] ?: getDisplayName(n)
    }

}
