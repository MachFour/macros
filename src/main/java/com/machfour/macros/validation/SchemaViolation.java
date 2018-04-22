package com.machfour.macros.validation;

import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.data.Column;

import java.util.Map;

public class SchemaViolation extends RuntimeException {
    public SchemaViolation(Column<?, ?> c, ValidationError v) {
        super("Schema error (" + v + ") found in column: " + c.sqlName());
    }

    public <M extends MacrosPersistable> SchemaViolation(Map<Column<M, ?>, ValidationError> errorMap) {
        super("Schema errors found:" + errorMap.toString());
    }
}
