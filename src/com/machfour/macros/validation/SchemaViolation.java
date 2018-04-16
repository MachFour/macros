package com.machfour.macros.validation;

import com.machfour.macros.data.Column;

import java.util.Map;

public class SchemaViolation extends RuntimeException {
    public SchemaViolation(Column<?> c, ValidationError v) {
        super("Schema error (" + v + ") found in column: " + c.sqlName());
    }

    public SchemaViolation(Map<Column<?>, ValidationError> errorMap) {
        super("Schema errors found:" + errorMap.toString());
    }
}
