package com.machfour.macros.entities.auxiliary

import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.validation.Validation
import com.machfour.macros.validation.ValidationError

// custom validation code for objects which cannot be expressed as a single-column constraint
private fun <M> makeErrorMap() = HashMap<Column<M, *>, List<ValidationError>>()


val nutrientValueValidations = object: Validation<FoodNutrientValue> {
    override val relevantColumns: Set<Column<FoodNutrientValue, *>> = setOf(
        FoodNutrientValueTable.NUTRIENT_ID, FoodNutrientValueTable.VALUE
    )

    override fun validate(
        data: RowData<FoodNutrientValue>
    ): Map<Column<FoodNutrientValue, *>, List<ValidationError>> {
        val nutrientCol = FoodNutrientValueTable.NUTRIENT_ID
        val valueCol = FoodNutrientValueTable.VALUE

        val nutrientId = data[nutrientCol]
        val value = data[valueCol]

        return makeErrorMap<FoodNutrientValue>().also {
            if (nutrientId == null) {
                // nutrient must be specified
                it[nutrientCol] = listOf(ValidationError.NON_NULL)
            } else if (nutrientId == QUANTITY.id) {
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