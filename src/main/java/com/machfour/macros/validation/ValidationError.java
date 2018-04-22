package com.machfour.macros.validation;

public enum ValidationError {
    NON_NULL("non-null"), TYPE_MISMATCH("type mismatch"), DATA_NOT_FOUND("data not found");

    final String str;

    ValidationError(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
