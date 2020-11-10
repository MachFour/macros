package com.machfour.macros.validation

import com.machfour.macros.core.Column

class SchemaViolation : RuntimeException {
    constructor (c: Column<*, *>, v: ValidationError) : super("Schema error (" + v + ") found in column: " + c.sqlName)
    constructor(errorMap: Map<out Column<*, *>, List<ValidationError>>) : super("Schema errors found:$errorMap")
}