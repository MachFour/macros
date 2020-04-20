package com.machfour.macros.core.datatype;

/*
 * Exception to be thrown when casting from one MacrosType to another fails.
 * Main use case: converting from a (raw) string to an actual Type
 */
public class TypeCastException extends Exception {
    public TypeCastException() {
    }

    public TypeCastException(String message) {
        super(message);
    }
}
