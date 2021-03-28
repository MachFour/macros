package com.machfour.macros.validation

enum class ValidationError(private val str: String) {
    NON_NULL("non-null"),
    TYPE_MISMATCH("type mismatch"),
    DATA_NOT_FOUND("data not found"),
    UNIQUE("value must be unique"),
    POSITIVE("value must be positive"),
    NON_NEGATIVE("value cannot be negative"),
    MUST_BE_NULL("value must be null"),
    ;

    override fun toString(): String = str
}