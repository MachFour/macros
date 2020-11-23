package com.machfour.macros.validation

enum class ValidationError(private val str: String) {
    NON_NULL("non-null"),
    TYPE_MISMATCH("type mismatch"),
    DATA_NOT_FOUND("data not found"),
    UNIQUE("value must be unique")
    ;

    override fun toString(): String = str
}