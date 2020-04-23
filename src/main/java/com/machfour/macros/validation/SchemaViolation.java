package com.machfour.macros.validation;

import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.Column;

import java.util.List;
import java.util.Map;

public class SchemaViolation extends RuntimeException {
    public SchemaViolation(Column<?, ?> c, ValidationError v) {
        super("Schema error (" + v + ") found in column: " + c.sqlName());
    }

    public <M extends MacrosEntity> SchemaViolation(Map<Column<M, ?>, List<ValidationError>> errorMap) {
        super("Schema errors found:" + errorMap.toString());
    }
}
