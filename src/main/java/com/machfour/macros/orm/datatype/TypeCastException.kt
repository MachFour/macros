package com.machfour.macros.orm.datatype

/*
 * Exception to be thrown when casting from one MacrosType to another fails.
 * Main use case: converting from a (raw) string to an actual Type
 */
class TypeCastException(message: String) : Exception(message)