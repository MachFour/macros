package com.machfour.macros.validation

import com.machfour.macros.core.Column

interface Validation {
    /*
     * Returns true if the string value for the given field would pass the given com.machfour.macros.validation test,
     * assuming that the value can be cast to its appropriate type first. Otherwise, returns false.
     * An exception is thrown if the value cannot be cast to the required type for the test,
     * or if the specified field is not a key in the stringValues map.
     * The whole map is required so that multi-field com.machfour.macros.validation can be done
     */
    fun <M> validate(stringValues: Map<Column<M, *>, String?>, field: Column<M, *>): Boolean
}