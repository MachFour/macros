package com.machfour.macros.validation

enum class ValidationError(private val str: String) {
    NON_NULL("non-null"),
    TYPE_MISMATCH("type mismatch"),

    override fun toString(): String = str
}