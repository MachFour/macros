package com.machfour.macros.validation

import com.machfour.macros.sql.Column

class SchemaViolation(errorMap: Map<out Column<*, *>, List<ValidationError>>) :
    IllegalArgumentException("Schema errors found: $errorMap") {
}