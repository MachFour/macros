package com.machfour.macros.validation

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.schema.AllTables
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

// custom validation code for objects which cannot be expressed as a single-column constraint
private fun <M> makeErrorMap() = HashMap<Column<M, *>, List<ValidationError>>()

// Just checks non null constraints
private open class DefaultValidator<M>(
    final override val table: Table<M>
) : MacrosValidator<M> {

    final override fun validateAsTableRow(data: RowData<M>): ErrorMap<M> {
        val dataErrors = validateData(data)
        val missingColumns = table.columns
            .subtract(data.columns)
            .filter { !it.isNullable && it.defaultData == null }
        return dataErrors + missingColumns.associateWith { listOf(ValidationError.DATA_NOT_FOUND) }
    }

    protected fun <J> validateNonNull(data: RowData<M>, col: Column<M, J>): List<ValidationError> {
        val isInvalid = data[col] == null && !col.isNullable
        return if (isInvalid) listOf(ValidationError.NON_NULL) else emptyList()
    }

    override fun validateData(data: RowData<M>): ErrorMap<M> {
        return data.columns
            .associateWith { validateNonNull(data, it) }
            .filterValues { it.isNotEmpty() }
    }

    // Just checks non null constraints
    // TODO add check for unique: needs DB access
    override fun <J> validateSingle(data: RowData<M>, col: Column<M, J>): List<ValidationError> {
        return validateNonNull(data, col)
    }
}

private val defaultValidators: Map<Table<out MacrosEntity<*>>, MacrosValidator<out MacrosEntity<*>>> by lazy {
    AllTables.associateWith { DefaultValidator(it) }
}

@Suppress("UNCHECKED_CAST")
fun <M: MacrosEntity<M>> tableValidator(table: Table<M>): MacrosValidator<M> {
    return requireNotNull(defaultValidators[table]) {
        "Unknown table $table. Is it added to AllTables?"
    } as MacrosValidator<M>
}

// allows null values for non-quantity columns
val NutrientValueInputValidator: MacrosValidator<FoodNutrientValue> =
    object: DefaultValidator<FoodNutrientValue>(FoodNutrientValueTable) {

    override fun validateData(data: RowData<FoodNutrientValue>): ErrorMap<FoodNutrientValue> {
        val nutrientCol = FoodNutrientValueTable.NUTRIENT_ID
        val valueCol = FoodNutrientValueTable.VALUE

        val nutrientId = data[nutrientCol]
        val value = data[valueCol]

        val errors = super.validateData(data).toMutableMap()

        if (nutrientId == QUANTITY.id) {
            // value not null would be checked before
            if (value != null && value <= 0) {
                errors[valueCol] = listOf(ValidationError.POSITIVE)
            }
        } else {
            // can be null but can't be negative
            errors[valueCol] = if (value != null && value < 0) listOf(ValidationError.NON_NEGATIVE) else emptyList()
        }
        return errors
    }

    override fun <J> validateSingle(
        data: RowData<FoodNutrientValue>,
        col: Column<FoodNutrientValue, J>
    ): List<ValidationError> {
        return validateData(data).getOrDefault(col, emptyList())
    }

}
