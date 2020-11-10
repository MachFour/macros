package com.machfour.macros.validation

enum class ValidationError(private val str: String) {
    NON_NULL("non-null"), TYPE_MISMATCH("type mismatch"), DATA_NOT_FOUND("data not found");

    override fun toString(): String {
        return str
    }

}