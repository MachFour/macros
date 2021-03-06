package com.machfour.macros.objects.helpers

import com.machfour.macros.core.Column
import com.machfour.macros.core.schema.SchemaHelpers
import com.machfour.macros.core.VErrorList
import com.machfour.macros.core.schema.FoodNutrientValueTable
import com.machfour.macros.objects.FoodNutrientValue
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.validation.Validation
import com.machfour.macros.validation.ValidationError

object Validations {
    // custom validation code for objects which cannot be expressed as a single-column constraint
    private fun <M> makeErrorMap() = HashMap<Column<M, *>, VErrorList>()


    val nutrientValue = Validation<FoodNutrientValue> { data ->
        val valueCol = FoodNutrientValueTable.VALUE
        val nutrientCol = FoodNutrientValueTable.NUTRIENT_ID

        val nutrientId = data[nutrientCol]
        val value = data[valueCol]

        makeErrorMap<FoodNutrientValue>().also {
            if (nutrientId == null) {
                // nutrient must be specified
                it[nutrientCol] = listOf(ValidationError.NON_NULL)
            } else if (nutrientId == Nutrients.QUANTITY.id) {
                if (value == null) {
                    it[valueCol] = listOf(ValidationError.NON_NULL)
                } else if (value <= 0) {
                    // quantity must be provided
                    it[valueCol] = listOf(ValidationError.POSITIVE)
                }
            } else {
                // can be null but can't be negative
                if (value != null && value < 0) {
                    it[valueCol] = listOf(ValidationError.NON_NEGATIVE)
                }

            }
        }
    }
}
